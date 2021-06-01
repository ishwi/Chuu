package core.otherlisteners;

import core.commands.Context;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static core.otherlisteners.Reactions.LEFT_ARROW;
import static core.otherlisteners.Reactions.RIGHT_ARROW;

public class ButtonValidator<T> extends ReactionListener {

    private final Function<EmbedBuilder, EmbedBuilder> getLastMessage;
    private final Supplier<T> elementFetcher;
    private final BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder;
    private final long whom;
    private final Context context;
    private final Map<String, Reaction<T, ButtonClickEvent, ButtonResult>> actionMap;
    private final boolean allowOtherUsers;
    private final boolean renderInSameElement;
    private final Queue<ButtonClickEvent> tbp = new LinkedBlockingDeque<>();
    private final AtomicBoolean hasCleaned = new AtomicBoolean(false);
    private List<ActionRow> actionRows;
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
                           boolean renderInSameElement, int timeout) {
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
        this(getLastMessage, elementFetcher, fillBuilder, who, context, discordId, actionMap, actionRows, allowOtherUsers, renderInSameElement, 30);
    }

    @org.jetbrains.annotations.NotNull
    public static ButtonResult rightMove(int size, AtomicInteger counter, ButtonClickEvent r, boolean isSame, List<ActionRow> baseRows) {
        int i = counter.incrementAndGet();
        List<ActionRow> rows = baseRows;
        List<Component> arrowLess = rows.get(0).getComponents().stream().filter(z -> !(z.getId().equals(LEFT_ARROW) || z.getId().equals(RIGHT_ARROW))).collect(Collectors.toCollection(ArrayList::new));
        arrowLess.add(Button.primary(LEFT_ARROW, Emoji.fromUnicode(LEFT_ARROW)));
        rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
        if (i != size - 1) {
            arrowLess.add(Button.primary(RIGHT_ARROW, Emoji.fromUnicode(RIGHT_ARROW)));
            rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
        }
        if (i != 1 && i != size - 1) {
            rows = null;
        }
        List<ActionRow> finalActionRows = rows;
        return () -> new ButtonResult.Result(false, finalActionRows);
    }

    @org.jetbrains.annotations.NotNull
    public static ButtonResult leftMove(int size, AtomicInteger counter, ButtonClickEvent r, boolean isSame, List<ActionRow> baseRows) {
        int i = counter.decrementAndGet();
        List<ActionRow> rows = baseRows;
        List<Component> arrowLess = rows.get(0).getComponents().stream().filter(z -> !(z.getId().equals(LEFT_ARROW) || z.getId().equals(RIGHT_ARROW))).collect(Collectors.toCollection(ArrayList::new));

        if (i != 0) {
            arrowLess.add(Button.primary(LEFT_ARROW, Emoji.fromUnicode(LEFT_ARROW)));
            rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
        }

        arrowLess.add(Button.primary(RIGHT_ARROW, Emoji.fromUnicode(RIGHT_ARROW)));
        rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
        List<ActionRow> finalActionRows = rows;
        return () -> new ButtonResult.Result(false, finalActionRows);
    }

    private void clearButtons() {
    }

    private void noMoreElements() {
        RestAction<Message> a;
        if (hasCleaned.compareAndSet(false, true)) {
            boolean check;
            if (message == null) {
                check = true;
                a = context.sendMessage(getLastMessage.apply(who).build(), Collections.emptyList());
            } else {
                check = false;
                a = message.editMessage(getLastMessage.apply(who).build()).setActionRows(Collections.emptyList());
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
        if (result.newRows() != null) {
            actionRows = result.newRows();
        }
        if (result.newElement() || this.message == null) {
            return context.sendMessage(apply.build(), actionRows);
        }
        return this.message.editMessage(new MessageBuilder(apply.build()).setActionRows(actionRows).build());
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof ButtonClickEvent e) {
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
                onEvent(poll);
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
        event.deferEdit().queue();
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
