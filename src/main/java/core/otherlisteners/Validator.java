package core.otherlisteners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Validator<T> extends ReactionListener {

    private final Function<EmbedBuilder, EmbedBuilder> getLastMessage;
    private final Supplier<T> elementFetcher;
    private final BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder;
    private final long whom;
    private final MessageChannel messageChannel;
    private final Map<String, BiFunction<T, MessageReactionAddEvent, Boolean>> actionMap;
    private final boolean allowOtherUsers;
    private final boolean renderInSameElement;
    private final Queue<MessageReactionAddEvent> tbp = new LinkedBlockingDeque<>();
    private final AtomicBoolean hasCleaned = new AtomicBoolean(false);
    private T currentElement;

    public Validator(UnaryOperator<EmbedBuilder> getLastMessage, Supplier<T> elementFetcher, BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder, EmbedBuilder who, MessageChannel channel, long discordId, Map<String, BiFunction<T, MessageReactionAddEvent, Boolean>> actionMap, boolean allowOtherUsers, boolean renderInSameElement) {
        super(who, null, 30, channel.getJDA());
        this.getLastMessage = getLastMessage;
        this.elementFetcher = elementFetcher;
        this.fillBuilder = fillBuilder;
        this.messageChannel = channel;
        this.whom = discordId;
        this.actionMap = actionMap;
        this.allowOtherUsers = allowOtherUsers;
        this.renderInSameElement = renderInSameElement;

        init();
    }


    private void noMoreElements() {
        RestAction<Message> a;
        if (hasCleaned.compareAndSet(false, true)) {
            boolean check;
            if (message == null) {
                check = true;
                a = messageChannel.sendMessage(getLastMessage.apply(who).build());
            } else {
                check = false;
                a = message.editMessage(getLastMessage.apply(who).build());
            }
            a.queue(z -> {
                if (check) {
                    message = z;
                }
                clearReacts();
            });
            this.unregister();
        }
    }

    @SuppressWarnings("rawtypes")
    private void initEmotes() {
        List<RestAction<Void>> reacts = this.actionMap.keySet().stream().map(x -> message.addReaction(x)).collect(Collectors.toList());
        RestAction.allOf(reacts).queue();
    }

    private MessageAction doTheThing(boolean newElement) {
        T t = elementFetcher.get();
        if (t == null) {
            noMoreElements();
            return null;
        }
        this.currentElement = t;
        return dotheLogicThing(t, newElement);
    }

    private MessageAction dotheLogicThing(T t, boolean newElement) {
        EmbedBuilder apply = fillBuilder.apply(t, who);
        if (newElement || this.message == null) {
            return messageChannel.sendMessage(apply.build());
        }
        return this.message.editMessage(apply.build());
    }


    @Override
    public void init() {
        MessageAction messageAction = doTheThing(true);
        if (messageAction == null) {
            return;
        }
        messageAction.queue(z -> {
            this.message = z;
            while (!tbp.isEmpty()) {
                MessageReactionAddEvent poll = tbp.poll();
                onMessageReactionAdd(poll);
            }
            initEmotes();
        });
    }

    @Override
    public void dispose() {
        noMoreElements();
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (this.message == null) {
            return;
        }
        if (event.getMessageIdLong() != message.getIdLong() || (!this.allowOtherUsers && event.getUserIdLong() != whom) ||
                event.getUserIdLong() == event.getJDA().getSelfUser().getIdLong() || !event.getReaction().getReactionEmote().isEmoji())
            return;
        BiFunction<T, MessageReactionAddEvent, Boolean> action = this.actionMap.get(event.getReaction().getReactionEmote().getAsCodepoints());
        if (action == null)
            return;
        Boolean apply = action.apply(currentElement, event);
        MessageAction messageAction = this.doTheThing(apply);
        if (messageAction != null) {
            if (Boolean.TRUE.equals(apply)) {
                messageAction.queue(this::accept);
            } else if (event.getUser() != null) {
                clearOneReact(event);
                if (renderInSameElement) {
                    messageAction.queue();
                }
            } else {
                messageAction.queue();
            }
        }
        refresh(event.getJDA());
    }

    private void accept(Message mes) {
        this.message.delete().queue(t -> {
            this.message = mes;
            this.initEmotes();
        });
    }

}
