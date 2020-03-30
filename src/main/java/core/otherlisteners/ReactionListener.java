package core.otherlisteners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public abstract class ReactionListener extends ListenerAdapter {
    private final static String PERMS_MES = "Don't have permissions to clear reactions :(\nYou can still manually remove the reaction\n";
    private final static String DMs_MES = "Can't clear reactions on dm's, please manually remove the reaction\n";
    public final EmbedBuilder who;
    public Message message;

    public ReactionListener(EmbedBuilder who, Message message) {
        this.who = who;
        this.message = message;
    }

    public abstract void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event);

    public void clearReacts() {
        try {

            if (message.isFromGuild()) {
                message.clearReactions().queue(aVoid -> {
                }, throwable -> message.editMessage(who.setFooter(PERMS_MES).build()).queue());
            }
        } catch (Exception ex) {
            message.editMessage(who.setFooter(PERMS_MES).build()).queue();
        }
    }

    public void clearOneReact(MessageReactionAddEvent event) {
        User user = event.getUser();
        assert user != null;
        if (event.isFromGuild()) {
            try {
                event.getReaction().removeReaction(event.getUser()).queue(aVoid -> {
                }, throwable ->
                {
                    MessageEmbed.Footer footer = message.getEmbeds().get(0).getFooter();
                    message.editMessage(who.setFooter(PERMS_MES + (footer != null ? footer.getText() : "")).build()).queue();
                });
            } catch (Exception ex) {
                MessageEmbed.Footer footer = message.getEmbeds().get(0).getFooter();
                message.editMessage(who.setFooter(PERMS_MES + (footer != null ? footer.getText() : "")).build()).queue();
            }
        } else {
            MessageEmbed.Footer footer = message.getEmbeds().get(0).getFooter();
            message.editMessage(who.setFooter(DMs_MES + (footer != null ? footer.getText() : "")).build()).queue();
        }
    }
}
