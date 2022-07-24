package test.runner;

import net.dv8tion.jda.api.events.GenericEvent;
import test.commands.parsers.EventEmitter;
import test.commands.utils.TestResources;

public record IntegrationRunner(GenericEvent event, EventEmitter publisher) implements AssertionRunner {

    @Override
    public void handle(GenericEvent e) {
        TestResources.callEvent(e);
    }

}
