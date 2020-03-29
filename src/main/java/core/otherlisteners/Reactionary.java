package core.otherlisteners;

import core.Chuu;
import core.commands.CommandUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

public class Reactionary<T> extends ListenerAdapter {
    private final Message message;
    private final EmbedBuilder who;
    private final int pageSize;
    private final List<T> list;
    private int counter = 0;
    private final boolean numberedEntries;

    public Reactionary(List<T> list, Message message, EmbedBuilder who) {
        this(list, message, 10, who);
    }


    public Reactionary(List<T> list, Message messageToReact, EmbedBuilder who, boolean numberedEntries) {
        this(list, messageToReact, 10, who, numberedEntries);
    }

    public Reactionary(List<T> list, Message messageToReact, int pageSize, EmbedBuilder who) {
        this(list, messageToReact, pageSize, who, true);
    }

    public Reactionary(List<T> list, Message messageToReact, int pageSize, EmbedBuilder who, boolean numberedEntries) {
        this.who = who;
        this.list = list;
        this.message = messageToReact;
        this.pageSize = pageSize;
        this.numberedEntries = numberedEntries;
        initReactionary(messageToReact, list, messageToReact.getJDA());

    }

    private void initReactionary(Message message, List<T> list, JDA jda) {
        if (list.size() <= 10)
            return;
        message.addReaction("U+2B05").submit();
        message.addReaction("U+27A1").submit();

        jda.addEventListener(this);
        try {
            Thread.sleep(40000);
        } catch (InterruptedException ex) {
            Chuu.getLogger().warn(ex.getMessage(), ex);
        }
        jda.removeEventListener(this);
        message.clearReactions().queue();
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
        who.setColor(CommandUtil.randomColor());
        message.editMessage(who.build()).queue();

    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getMessageIdLong() != message.getIdLong() || (event.getUser() != null && event.getUser().isBot()))
            return;
        int start;
        switch (event.getReaction().getReactionEmote().getAsCodepoints()) {
            case "U+2b05":
                start = max(0, counter - pageSize);
                break;
            case "U+27a1":
                start = min(list.size() - (list.size() % pageSize), counter + pageSize);
                break;
            default:
                return;
        }
        event.getReaction().removeReaction(event.getUser()).queue(aVoid -> {
        }, throwable ->
        {
            message.editMessage(who.setFooter("Don't have permissions to clear reactions :(").build()).queue();
            event.getJDA().removeEventListener(this);
        });
        StringBuilder a = new StringBuilder();
        for (int i = start; i < start + pageSize && i < list.size(); i++) {
            if (numberedEntries) {
                a.append(i + 1);
            }
            a.append(list.get(i).toString());

        }
        counter = start;
        who.setDescription(a);
        who.setColor(CommandUtil.randomColor());
        message.editMessage(who.build()).queue();
    }

}
