package core.commands;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class ContextSlashReceived extends InteracionReceived<SlashCommandInteractionEvent> implements Context {
    public ContextSlashReceived(SlashCommandInteractionEvent e) {
        super(e);
    }

    @Override
    public MessageChannel getChannel() {
        return e.getChannel();
    }
}
