package core.otherlisteners;

import core.Chuu;
import core.commands.CustomInterfacedEventManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class ReactionListener implements EventListener {

    private static final String PERMS_MES = "Don't have permissions to clear reactions :(\nYou can still manually remove the reaction\n";
    private static final String DMS_MES = "Can't clear reactions on dms, please manually remove the reaction\n";
    public final EmbedBuilder who;
    public final JDA jda;
    public final long channelId;
    private final long activeSeconds;
    public Message message;

    public ReactionListener(EmbedBuilder who, Message message) {
        this(who, message, 30, message.getJDA(), message.getChannel().getIdLong());
    }

    public ReactionListener(EmbedBuilder who, Message message, long activeSeconds, JDA jda, long channelId) {
        this.who = who;
        this.message = message;
        this.activeSeconds = activeSeconds;
        this.jda = jda;
        this.channelId = channelId;
        register();
    }

    public ReactionListener(EmbedBuilder who, Message message, long activeSeconds, long channelId) {
        this(who, message, activeSeconds, message.getJDA(), channelId);

    }


    @Override
    public void onEvent(@NotNull GenericEvent event) {
        switch (event) {
            case MessageReactionAddEvent e -> {
                if (isValid(e)) {
                    onMessageReactionAdd(e);
                }
            }
            case ButtonInteractionEvent e -> {
                if (isValid(e)) {
                    onButtonClickedEvent(e);
                }
            }
            case StringSelectInteractionEvent e -> {
                if (isValid(e)) {
                    onSelectedMenuEvent(e);
                }
            }
            default -> {
            }
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

    public abstract boolean isValid(MessageReactionAddEvent event);

    public abstract boolean isValid(ButtonInteractionEvent event);

    public abstract boolean isValid(StringSelectInteractionEvent event);

    public abstract void dispose();

    public abstract void onMessageReactionAdd(@NotNull MessageReactionAddEvent event);

    public abstract void onButtonClickedEvent(@NotNull ButtonInteractionEvent event);

    public abstract void onSelectedMenuEvent(@NotNull StringSelectInteractionEvent event);

    public void clearReacts() {
        clearReacts((Void a) -> {
        });
    }

    public void clearReacts(Consumer<Void> consumer) {
        try {

            if (message.isFromGuild()) {
                message.clearReactions().queue(consumer,
                        throwable -> message.editMessageEmbeds(who.setFooter(PERMS_MES).build()).queue());
            }
        } catch (Exception ex) {
            message.editMessageEmbeds(who.setFooter(PERMS_MES).build()).queue();
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
                    message.editMessageEmbeds(who.setFooter(PERMS_MES + (footer != null ? footer.getText() : "")).build()).queue();
                });
            } catch (Exception ex) {
                MessageEmbed.Footer footer = message.getEmbeds().get(0).getFooter();
                message.editMessageEmbeds(who.setFooter(PERMS_MES + (footer != null ? footer.getText() : "")).build()).queue();
            }
        } else {
            MessageEmbed.Footer footer = message.getEmbeds().get(0).getFooter();
            message.editMessageEmbeds(who.setFooter(DMS_MES + (footer != null ? footer.getText() : "")).build()).queue();
        }
    }

    public long getActiveSeconds() {
        return activeSeconds;
    }


}
