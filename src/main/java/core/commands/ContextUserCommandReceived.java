package core.commands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public final class ContextUserCommandReceived extends InteracionReceived<UserContextInteractionEvent> implements Context {
    public ContextUserCommandReceived(UserContextInteractionEvent e) {
        super(e);
    }

    @Override
    public MessageChannel getChannel() {
        return e.getMessageChannel();
    }


}
