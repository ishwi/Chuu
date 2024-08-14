package core.commands;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class ContextSlashReceived extends InteracionReceived<SlashCommandInteractionEvent> implements Context {
    public ContextSlashReceived(SlashCommandInteractionEvent e) {
        super(e);
    }

    @Override
    public MessageChannelUnion getChannelUnion() {
        return e.getChannel();
    }
}
