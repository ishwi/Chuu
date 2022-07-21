package core.otherlisteners;

import core.commands.utils.ButtonUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

public class Reactionary<T> extends ReactionListener {
    private final int pageSize;
    private final List<T> list;
    private final boolean numberedEntries;
    private final boolean pagingIndicator;
    private final Function<Integer, String> extraText;
    private final Function<T, String> mapper;
    private final Function<List<T>, ActionRow> extraRowGen;
    private int counter = 0;
    private boolean missingArrow = true;


    public Reactionary(List<T> list, Message messageToReact, int pageSize, EmbedBuilder who, boolean numberedEntries, boolean pagingIndicator, long seconds, Function<T, String> mapper, Function<Integer, String> extraText, Function<List<T>, ActionRow> extraRow) {
        super(who, messageToReact);
        this.list = list;
        this.pageSize = pageSize;
        this.numberedEntries = numberedEntries;
        this.pagingIndicator = pagingIndicator;
        this.mapper = mapper;
        this.extraText = extraText;
        this.extraRowGen = extraRow;
        init();
    }


    @Override
    public void init() {
        if (list.size() <= pageSize) {
            unregister();
        }
    }

    @Override
    public boolean isValid(MessageReactionAddEvent event) {
        if (message == null) {
            return false;
        }
        return event.getMessageIdLong() == message.getIdLong() && ((event.getUser() == null || !event.getUser().isBot()));
    }

    @Override
    public boolean isValid(ButtonInteractionEvent event) {
        return message != null && event.getMessageIdLong() == message.getIdLong();
    }

    @Override
    public boolean isValid(SelectMenuInteractionEvent event) {
        return false;
    }

    @Override
    public void dispose() {
        if (counter >= pageSize) {
            StringBuilder a = new StringBuilder();
            for (int i = 0; i < pageSize && i < list.size(); i++) {
                if (this.numberedEntries) {
                    a.append(i + 1);
                }
                a.append(mapper.apply(list.get(i)));
            }
            who.setDescription(a);
        }
        message.editMessageEmbeds(who.build()).setActionRows(Collections.emptyList()).queue();
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {

    }

    @Override
    public void onButtonClickedEvent(@Nonnull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        if (message == null) {
            return;
        }
        if (event.getMessageIdLong() != message.getIdLong() || event.getUser().isBot())
            return;
        int start;
        String id = event.getComponentId();
        switch (id) {
            case Reactions.LEFT_ARROW:
                start = max(0, counter - pageSize);
                break;
            case Reactions.RIGHT_ARROW:
                start = min(list.size() - (list.size() % pageSize), counter + pageSize);
                break;
            default:
                return;
        }

        int currentPage = (int) Math.ceil(start / (float) pageSize) + 1;
        int totalPageNumber = (int) Math.ceil(list.size() / (float) pageSize);
        StringBuilder a = new StringBuilder();


        for (int i = start; i < start + pageSize && i < list.size(); i++) {
            if (numberedEntries) {
                a.append(i + 1);
            }
            a.append(mapper.apply(list.get(i)));

        }

        if (pagingIndicator) {
            a.append("\n ").append(currentPage).append("/").append(totalPageNumber);
        }
        if (extraText != null) {
            a.append("\n").append(extraText.apply(currentPage));
        }
        counter = start;
        who.setDescription(a);

        ActionRow actionRow = ActionRow.of(ButtonUtils.getLeftButton(), ButtonUtils.getRightButton());
        if (currentPage == 1 && id.equals(Reactions.LEFT_ARROW)) {
            actionRow = ActionRow.of(ButtonUtils.getRightButton());
        } else if (currentPage == 2 && id.equals(Reactions.RIGHT_ARROW) && missingArrow) {
            if (totalPageNumber == 2) {
                actionRow = ActionRow.of(ButtonUtils.getLeftButton());
            }
        } else if (currentPage == totalPageNumber && id.equals(Reactions.RIGHT_ARROW)) {
            actionRow = ActionRow.of(ButtonUtils.getLeftButton());
        } else if (currentPage == totalPageNumber - 1 && id.equals(Reactions.LEFT_ARROW) && missingArrow) {
            actionRow = ActionRow.of(ButtonUtils.getLeftButton(), ButtonUtils.getRightButton());
        }

        missingArrow = actionRow.getButtons().size() != 2;
        refresh(event.getJDA());
        List<ActionRow> rows = List.of(actionRow);

        List<T> items = list.subList(start, min(start + pageSize, list.size()));
        ActionRow extraRow = null;
        if (extraRowGen != null) {
            extraRow = extraRowGen.apply(items);
        }
        if (actionRow.isEmpty()) {
            if (extraRow != null)
                rows = List.of(extraRow);
        } else {
            if (extraRow != null) {
                rows = List.of(actionRow, extraRow);
            }
        }
        message.editMessage(new MessageBuilder().setEmbeds(who.build()).setActionRows(rows).build()).queue();
    }

    @Override
    public void onSelectedMenuEvent(@NotNull SelectMenuInteractionEvent event) {

    }
}


