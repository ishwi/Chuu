package core.otherlisteners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class Validator<T> extends ReactionListener {

    private final Function<EmbedBuilder, EmbedBuilder> getLastMessage;
    private final Supplier<T> elementFetcher;
    private final BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder;
    private final long whom;
    private final MessageChannel messageChannel;
    private final Map<String, BiFunction<T, MessageReactionAddEvent, Boolean>> actionMap;
    private T currentElement;
    private int counter = 0;
    private final boolean allowOtherUsers;
    private final boolean renderInSameElement;

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


    private void endItAll(JDA jda) {
        jda.removeEventListener(this);
        clearReacts();
    }

    private void noMoreElements(JDA jda) {
        if (message == null) {
            this.message = messageChannel.sendMessage(getLastMessage.apply(who).build()).complete();
        } else
            message.editMessage(getLastMessage.apply(who).build()).complete();
        endItAll(jda);
    }

    @SuppressWarnings("rawtypes")
    private void initEmotes() {
        CompletableFuture[] completableFutures1 = this.actionMap.keySet().stream().map(x -> message.addReaction(x).submit()).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures1).join();
    }

    private MessageAction doTheThing(JDA jda, boolean newElement) {
        T t = elementFetcher.get();
        if (t == null) {
            noMoreElements(jda);
            return null;
        }
        this.currentElement = t;
        EmbedBuilder apply = fillBuilder.apply(t, who);
        if (newElement || this.message == null) {
            return messageChannel.sendMessage(apply.build());
        }
        return this.message.editMessage(apply.build());
    }


    @Override
    public void init() {
        MessageAction messageAction = doTheThing(messageChannel.getJDA(), true);
        if (messageAction == null) {
            return;
        }
        this.message = messageAction.complete();
        initEmotes();
    }

    @Override
    public void dispose() {
        noMoreElements(messageChannel.getJDA());

    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getMessageIdLong() != message.getIdLong() || (!this.allowOtherUsers && event.getUserIdLong() != whom) || event.getUserIdLong() == event.getJDA().getSelfUser().getIdLong())
            return;
        BiFunction<T, MessageReactionAddEvent, Boolean> action = this.actionMap.get(event.getReaction().getReactionEmote().getAsCodepoints());
        if (action == null)
            return;
        Boolean apply = action.apply(currentElement, event);
        MessageAction messageAction = this.doTheThing(event.getJDA(), apply);
        counter++;
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
