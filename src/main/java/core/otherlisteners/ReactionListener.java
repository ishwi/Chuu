package core.otherlisteners;

import core.Chuu;
import core.commands.CustomInterfacedEventManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class ReactionListener implements EventListener {

    private static final String PERMS_MES = "Don't have permissions to clear reactions :(\nYou can still manually remove the reaction\n";
    private static final String DMS_MES = "Can't clear reactions on dm's, please manually remove the reaction\n";
    public final EmbedBuilder who;
    public final JDA jda;
    private final long activeSeconds;
    public Message message;

    public ReactionListener(EmbedBuilder who, Message message) {
        this(who, message, 30);
    }

    public ReactionListener(EmbedBuilder who, Message message, long activeSeconds, JDA jda) {
        this.who = who;
        this.message = message;
        this.activeSeconds = activeSeconds;
        this.jda = jda;
        register();
    }

    public ReactionListener(EmbedBuilder who, Message message, long activeSeconds) {
        this(who, message, activeSeconds, message.getJDA());

    }


    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof MessageReactionAddEvent e) {
            onMessageReactionAdd(e);
        } else if (event instanceof ButtonClickEvent e) {
            onButtonClickedEvent(e);
        }
    }

    void register() {
        jda.getEventManager().register(this);
    }

    void unregister() {
        jda.getEventManager().unregister(this);
    }

    public void refresh(JDA jda) {
        IEventManager eventManager = jda.getEventManager();
        if (!(eventManager instanceof CustomInterfacedEventManager manager)) {
            Chuu.getLogger().error("Wrong type on Interface, You must have forgot about this thing");
            return;
        }
        manager.refreshReactionay(this, getActiveSeconds());
    }

    public abstract void init();

    public abstract void dispose();

    public abstract void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event);

    public abstract void onButtonClickedEvent(@Nonnull ButtonClickEvent event);

    public void clearReacts() {
        clearReacts((Void a) -> {
        });
    }

    public void clearReacts(Consumer<Void> consumer) {
        try {

            if (message.isFromGuild()) {
                message.clearReactions().queue(consumer,
                        throwable -> message.editMessage(who.setFooter(PERMS_MES).build()).queue());
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
            message.editMessage(who.setFooter(DMS_MES + (footer != null ? footer.getText() : "")).build()).queue();
        }
    }

    public long getActiveSeconds() {
        return activeSeconds;
    }


}
