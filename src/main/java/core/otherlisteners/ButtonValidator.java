package core.otherlisteners;

import core.commands.Context;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final Map<String, Reaction<T, ButtonInteractionEvent, ButtonResult>> actionMap;
    private final boolean allowOtherUsers;
    private final boolean renderInSameElement;
    private final AtomicBoolean hasCleaned = new AtomicBoolean(false);
    private List<ActionRow> actionRows;
    private T currentElement;


    public ButtonValidator(UnaryOperator<EmbedBuilder> getLastMessage, Supplier<T> elementFetcher, BiFunction<T, EmbedBuilder, EmbedBuilder> fillBuilder, EmbedBuilder who, Context context,
                           long discordId, Map<String, Reaction<T, ButtonInteractionEvent, ButtonResult>> actionMap, List<ActionRow> actionRows, boolean allowOtherUsers, boolean renderInSameElement, long channelId,
                           long activeSeconds
    ) {
        super(who, null, activeSeconds, context.getJDA(), channelId);
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

    @org.jetbrains.annotations.NotNull
    public static ButtonResult rightMove(int size, AtomicInteger counter, ButtonInteractionEvent r, boolean isSame, List<ActionRow> baseRows) {
        int i = counter.incrementAndGet();
        List<ActionRow> rows = baseRows;
        List<ActionComponent> arrowLess = rows.get(0).getActionComponents().stream().filter((ActionComponent z) -> !(Objects.equals(z.getId(), LEFT_ARROW) || Objects.equals(z.getId(), RIGHT_ARROW))).collect(Collectors.toCollection(ArrayList::new));
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

    public static <T> @NotNull Supplier<T> paginate(AtomicInteger counter, List<T> webhooks) {
        return () -> {
            if (counter.get() >= webhooks.size() - 1) {
                counter.set(webhooks.size() - 1);
            }
            if (counter.get() < 0) {
                counter.set(0);
            }
            if (webhooks.isEmpty()) {
                return null;
            }
            return webhooks.get(counter.get());
        };
    }


    @org.jetbrains.annotations.NotNull
    public static ButtonResult leftMove(int size, AtomicInteger counter, ButtonInteractionEvent r, boolean isSame, List<ActionRow> baseRows) {
        int i = counter.decrementAndGet();
        List<ActionRow> rows = baseRows;
        List<ItemComponent> arrowLess = rows.get(0).getActionComponents().stream().filter(z -> !(Objects.equals(z.getId(), LEFT_ARROW) || Objects.equals(z.getId(), RIGHT_ARROW))).collect(Collectors.toCollection(ArrayList::new));

        if (i != 0) {
            arrowLess.add(Button.primary(LEFT_ARROW, Emoji.fromUnicode(LEFT_ARROW)));
            rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
        }

        arrowLess.add(Button.primary(RIGHT_ARROW, Emoji.fromUnicode(RIGHT_ARROW)));
        rows = Stream.concat(Stream.of(ActionRow.of(arrowLess)), rows.stream().skip(1)).toList();
        List<ActionRow> finalActionRows = rows;
        return () -> new ButtonResult.Result(false, finalActionRows);
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
                a = context.editMessage(message, getLastMessage.apply(who).build(), Collections.emptyList());
            }
            a.queue(z -> {
                if (check) {
                    message = z;
                }
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
        return this.context.editMessage(message, apply.build(), actionRows);
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ButtonInteractionEvent e) {
            if (isValid(e)) {
                onButtonClickedEvent(e);
            }
        }
    }

    @Override
    public void init() {

        RestAction<Message> messageAction = doTheThing(() -> new ButtonResult.Result(true, null));
        if (messageAction == null) {
            return;
        }
        messageAction.queue(z -> this.message = z);
    }

    @Override
    public boolean isValid(MessageReactionAddEvent event) {
        return false;
    }

    @Override
    public boolean isValid(ButtonInteractionEvent event) {
        if (this.message == null) {
            return false;
        }
        if (event.getMessageIdLong() != message.getIdLong()) {
            return false;
        }
        return event.getMessageIdLong() == message.getIdLong() && (this.allowOtherUsers || event.getUser().getIdLong() == whom) && event.getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong();
    }

    @Override
    public boolean isValid(StringSelectInteractionEvent event) {
        return false;
    }

    @Override
    public void dispose() {
        noMoreElements();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onButtonClickedEvent(@NotNull ButtonInteractionEvent event) {
        if (!event.isAcknowledged()) {
            event.deferEdit().queue();
        }
        Reaction<T, ButtonInteractionEvent, ButtonResult> action = this.actionMap.get(event.getComponentId());
        if (action == null) return;
        ButtonResult apply = action.release(currentElement, event);
        RestAction<Message> messageAction = this.doTheThing(apply);
        if (messageAction != null) {
            if (apply.newResult().newElement()) {
                messageAction.queue(this::accept);
            } else {
                event.getUser();
                if (renderInSameElement) {
                    messageAction.queue();
                }
            }
        }
        refresh(event.getJDA());
    }

    @Override
    public void onSelectedMenuEvent(@NotNull StringSelectInteractionEvent event) {

    }

    private void accept(Message mes) {
        this.message.delete().queue(t -> this.message = mes);
    }

}
