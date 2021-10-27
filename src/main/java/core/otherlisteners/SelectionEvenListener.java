package core.otherlisteners;

import core.commands.Context;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public class SelectionEvenListener extends ReactionListener {
    private final boolean allowOtherUsers;
    private final BiFunction<EmbedBuilder, List<ActionRow>, SelectionResponse> getLastMessage;
    private final long whom;
    private final Context context;
    private final SelectionAction action;
    private EmbedBuilder currentEmbedBuilder;

    public SelectionEvenListener(EmbedBuilder who, Message message, boolean allowOtherUsers, BiFunction<EmbedBuilder, List<ActionRow>, SelectionResponse> getLastMessage, long whom, Context context, SelectionAction action) {
        super(who, message);
        this.allowOtherUsers = allowOtherUsers;
        this.getLastMessage = getLastMessage;
        this.whom = whom;
        this.context = context;
        this.action = action;
        this.currentEmbedBuilder = who;
    }

    @Override
    public void init() {

    }

    @Override
    public boolean isValid(MessageReactionAddEvent event) {
        return false;
    }

    @Override
    public boolean isValid(ButtonClickEvent event) {
        return false;
    }

    @Override
    public boolean isValid(SelectionMenuEvent event) {
        if (this.message == null) {
            return false;
        }
        if (event.getMessageIdLong() != message.getIdLong()) {
            return false;
        }
        return event.getMessageIdLong() == message.getIdLong() && (this.allowOtherUsers || event.getUser().getIdLong() == whom) &&
                event.getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong();
    }

    @Override
    public void dispose() {
        SelectionResponse sr = getLastMessage.apply(currentEmbedBuilder, message.getActionRows());

        context.editMessage(message, sr.embedBuilder == null ? null : sr.embedBuilder.build(), sr.rows).queue();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {

    }

    @Override
    public void onButtonClickedEvent(@NotNull ButtonClickEvent event) {

    }

    @Override
    public void onSelectedMenuEvent(@NotNull SelectionMenuEvent event) {
        event.deferEdit().queue();
        SelectionMenu component = event.getComponent();
        List<ActionRow> actionRows = event.getMessage().getActionRows();

        SelectionResponse sr = action.processEvent(context, component, event.getValues(), who, actionRows);
        this.currentEmbedBuilder = sr.embedBuilder;
        message.editMessageEmbeds(sr.embedBuilder.build()).setActionRows(sr.rows()).queue();

        refresh(event.getJDA());

    }

    public interface SelectionAction {
        SelectionResponse processEvent(Context e, SelectionMenu menu, List<String> selected, EmbedBuilder eb, List<ActionRow> actionRows);
    }

    public record SelectionResponse(EmbedBuilder embedBuilder, List<ActionRow> rows) {
    }
}
