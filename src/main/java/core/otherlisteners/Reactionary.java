package core.otherlisteners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.annotation.Nonnull;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

public class Reactionary<T> extends ReactionListener {
    private static final String RIGHT_ARROW = "U+27a1";
    private static final String LEFT_ARROW = "U+2b05";
    private final int pageSize;
    private final List<T> list;
    private final boolean numberedEntries;
    private final boolean pagingIndicator;
    private int counter = 0;
    private boolean missingArrow = true;


    public Reactionary(List<T> list, Message message, EmbedBuilder who) {

        this(list, message, 10, who);
    }


    public Reactionary(List<T> list, Message messageToReact, EmbedBuilder who, boolean numberedEntries) {
        this(list, messageToReact, 10, who, numberedEntries, false);
    }

    public Reactionary(List<T> list, Message messageToReact, int pageSize, EmbedBuilder who) {
        this(list, messageToReact, pageSize, who, true, false);
    }

    public Reactionary(List<T> list, Message messageToReact, int pageSize, EmbedBuilder who, boolean numberedEntries) {
        this(list, messageToReact, pageSize, who, numberedEntries, false);
    }


    public Reactionary(List<T> list, Message messageToReact, int pageSize, EmbedBuilder who, boolean numberedEntries, boolean pagingIndicator) {
        this(list, messageToReact, pageSize, who, numberedEntries, pagingIndicator, 40);
    }

    public Reactionary(List<T> list, Message messageToReact, int pageSize, EmbedBuilder who, boolean numberedEntries, boolean pagingIndicator, long seconds) {
        super(who, messageToReact, seconds);
        this.list = list;
        this.pageSize = pageSize;
        this.numberedEntries = numberedEntries;
        this.pagingIndicator = pagingIndicator;
        init();
    }


    @Override
    public void init() {
        if (list.size() <= pageSize)
            return;
        //message.addReaction(LEFT_ARROW).queue();
        message.addReaction(RIGHT_ARROW).queue();
    }

    @Override
    public void dispose() {
        clearReacts();
        StringBuilder a = new StringBuilder();
        if (counter < pageSize)
            return;

        for (int i = 0; i < pageSize && i < list.size(); i++) {
            if (this.numberedEntries) {
                a.append(i + 1);
            }
            a.append(list.get(i).toString());
        }
        who.setDescription(a);
//        who.setColor(CommandUtil.pastelColor());
        message.editMessage(who.build()).queue();
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (message == null) {
            return;
        }
        if (event.getMessageIdLong() != message.getIdLong() || (event.getUser() != null && event.getUser().isBot() || !event.getReaction().getReactionEmote().isEmoji()))
            return;
        int start;
        String asCodepoints = event.getReaction().getReactionEmote().getAsCodepoints();
        switch (asCodepoints) {
            case LEFT_ARROW:
                start = max(0, counter - pageSize);
                break;
            case RIGHT_ARROW:
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
            a.append(list.get(i).toString());

        }

        if (pagingIndicator) {
            a.append("\n ").append(currentPage).append("/").append(totalPageNumber);
        }
        counter = start;
        who.setDescription(a);
//        who.setColor(ColorService.computeColor(e));
        message.editMessage(who.build()).queue();
        clearOneReact(event);

        if (currentPage == 1 && asCodepoints.equals(LEFT_ARROW)) {
            message.removeReaction(LEFT_ARROW).queue();
            missingArrow = true;
        } else if (currentPage == 2 && asCodepoints.equals(RIGHT_ARROW) && missingArrow) {
            clearReacts((Void t) -> message.addReaction(LEFT_ARROW).queue(x -> {
                if (totalPageNumber != 2) {
                    message.addReaction(RIGHT_ARROW).queue();
                }
            }));
            missingArrow = false;
        }
        if (currentPage == totalPageNumber && asCodepoints.equals(RIGHT_ARROW)) {
            message.removeReaction(RIGHT_ARROW).queue();
            missingArrow = true;
        } else if (currentPage == totalPageNumber - 1 && asCodepoints.equals(LEFT_ARROW) && missingArrow) {
            message.addReaction(RIGHT_ARROW).queue();
            missingArrow = false;
        }

        refresh(event.getJDA());
    }

    @Override
    public void onButtonClickedEvent(@Nonnull ButtonClickEvent event) {

    }

}
