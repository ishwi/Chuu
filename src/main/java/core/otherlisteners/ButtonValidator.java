package core.otherlisteners;

import core.commands.Context;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;

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

public class ButtonValidator<T> extends ReactionListener {

    private final Function<EmbedBuilder, EmbedBuilder> getLastMessage;
    private final Supplier<T> elementFetcher;
    private final BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder;
    private final long whom;
    private final Context context;
    private final Map<String, Reaction<T, ButtonClickEvent, ButtonResult>> actionMap;
    private final List<ActionRow> actionRows;
    private final boolean allowOtherUsers;
    private final boolean renderInSameElement;
    private final Queue<ButtonClickEvent> tbp = new LinkedBlockingDeque<>();
    private final AtomicBoolean hasCleaned = new AtomicBoolean(false);
    private T currentElement;

    public ButtonValidator(UnaryOperator<EmbedBuilder> getLastMessage,
                           Supplier<T> elementFetcher,
                           BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder,
                           EmbedBuilder who,
                           Context context,
                           long discordId,
                           Map<String, Reaction<T, ButtonClickEvent, ButtonResult>> actionMap,
                           List<ActionRow> actionRows
            , boolean allowOtherUsers,
                           boolean renderInSameElement) {
        super(who, null, 30, context.getJDA());
        this.getLastMessage = getLastMessage;
        this.elementFetcher = elementFetcher;
        this.fillBuilder = fillBuilder;
        this.context = context;
        this.whom = discordId;
        this.actionMap = actionMap;
        this.actionRows = actionRows;
        this.allowOtherUsers = allowOtherUsers;
        this.renderInSameElement = renderInSameElement;

        init();
    }

    private void clearButtons() {

    }

    private void noMoreElements() {
        RestAction<Message> a;
        if (hasCleaned.compareAndSet(false, true)) {
            boolean check;
            if (message == null) {
                check = true;
                a = context.sendMessage(getLastMessage.apply(who).build());
            } else {
                check = false;
                a = message.editMessage(getLastMessage.apply(who).build());
            }
            a.queue(z -> {
                if (check) {
                    message = z;
                }
                clearButtons();
            });
            this.unregister();
        }
    }


    private RestAction<Message> doTheThing(ButtonResult newElement) {
        T t = elementFetcher.get();
        if (t == null) {
            noMoreElements();
            return null;
        }
        this.currentElement = t;
        return dotheLogicThing(t, newElement);
    }

    private RestAction<Message> dotheLogicThing(T t, ButtonResult newElement) {
        EmbedBuilder apply = fillBuilder.apply(t, who);
        ButtonResult.Result result = newElement.newResult();
        if (result.newElement() || this.message == null) {
            return context.sendMessage(apply.build(), result.newRows() == null ? actionRows : result.newRows());
        }
        return this.message.editMessage(new MessageBuilder(apply.build()).setActionRows(result.newRows() == null ? actionRows : result.newRows()).build());
    }


    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof ButtonClickEvent e) {
            if (!e.isAcknowledged()) {
                e.deferEdit().queue();
            }
            onButtonClickedEvent(e);
        }
    }

    @Override
    public void init() {

        RestAction<Message> messageAction = doTheThing(() -> new ButtonResult.Result(true, null));
        if (messageAction == null) {
            return;
        }
        messageAction.queue(z -> {
            this.message = z;
            while (!tbp.isEmpty()) {
                ButtonClickEvent poll = tbp.poll();
                onButtonClickedEvent(poll);
            }
        });
    }

    @Override
    public void dispose() {
        noMoreElements();
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onButtonClickedEvent(@Nonnull ButtonClickEvent event) {
        if (this.message == null) {
            return;
        }
        if (event.getMessageIdLong() != message.getIdLong()) {
            return;
        }
        if (event.getUser() == null)
            if (event.getMessageIdLong() != message.getIdLong() || (!this.allowOtherUsers && event.getUser().getIdLong() != whom) ||
                event.getUser().getIdLong() == event.getJDA().getSelfUser().getIdLong())
                return;
        Reaction<T, ButtonClickEvent, ButtonResult> action = this.actionMap.get(event.getComponentId());
        if (action == null)
            return;
        ButtonResult apply = action.release(currentElement, event);
        RestAction<Message> messageAction = this.doTheThing(apply);
        if (messageAction != null) {
            if (apply.newResult().newElement()) {
                messageAction.queue(this::accept);
            } else if (event.getUser() != null) {
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
        this.message.delete().queue(t -> this.message = mes);
    }

}
