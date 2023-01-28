package core.commands.abstracts;

import core.commands.ContextMessageReceived;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import test.commands.parsers.EventEmitter;
import test.runner.AssertionRunner;

public record CommandRunner(MyCommand<?> command, GenericEvent event,
                            EventEmitter publisher) implements AssertionRunner {

    @Override
    public void handle(GenericEvent e) {
        if (e instanceof MessageReceivedEvent mre) {
            command.onMessageReceived(new ContextMessageReceived(mre, false));
        }
    }

}
