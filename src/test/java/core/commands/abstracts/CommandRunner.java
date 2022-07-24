package core.commands.abstracts;

import net.dv8tion.jda.api.events.GenericEvent;
import test.commands.parsers.EventEmitter;
import test.runner.AssertionRunner;

public record CommandRunner(MyCommand<?> command, GenericEvent event,
                            EventEmitter publisher) implements AssertionRunner {

    @Override
    public void handle(GenericEvent e) {
        command.onEvent(event);
    }

}
