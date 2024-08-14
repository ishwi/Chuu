package test.runner;

import core.commands.abstracts.CommandRunner;
import core.commands.abstracts.MyCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import org.assertj.core.api.Assertions;
import test.commands.parsers.AssertionMode;
import test.commands.parsers.EventEmitter;
import test.commands.parsers.MessageGenerator;
import test.commands.parsers.TestAssertion;
import test.commands.parsers.factories.Factory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public interface AssertionRunner {

    static IntegrationRunner fromMessage(String message) {
        return fromMessage(message, Factory.def());
    }

    static IntegrationRunner fromMessage(String message, Factory factory) {
        MessageGenerator generator = new MessageGenerator(message, factory);
        return new IntegrationRunner(generator.event(), generator.publisher());
    }


    static CommandRunner fromCommand(MyCommand<?> command, String params) {
        return fromCommand(command, params, Factory.def());
    }


    static CommandRunner fromCommand(MyCommand<?> command, String message, Factory factory) {
        MessageGenerator generator = new MessageGenerator(message, factory);
        return new CommandRunner(command, generator.event(), generator.publisher());
    }

    void handle(GenericEvent e);

    GenericEvent event();

    EventEmitter publisher();

    default void assertion(List<TestAssertion<? extends EventEmitter.TestEvent>> assertions) {
        assertion(AssertionMode.ORDER_REQUIRED, assertions);
    }

    default void emptyAssertion() {
        assertion(AssertionMode.NOTHING_RECEIVED, Collections.emptyList());
    }

    default void assertion(AssertionMode mode, List<TestAssertion<? extends EventEmitter.TestEvent>> assertions) {
        if (mode == AssertionMode.NOTHING_RECEIVED) {
            try {
                Thread.sleep(2_000);
                assertThat(publisher().counter().get()).isEqualTo(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        handle(event());
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            AtomicInteger counter = new AtomicInteger();

            for (TestAssertion<? extends EventEmitter.TestEvent> assertion : assertions) {
                scope.fork(() -> {
                    publisher().runTest(assertion, counter.getAndIncrement(), mode);
                    return null;
                });
            }
            scope.join();
            scope.throwIfFailed();
        } catch (InterruptedException | ExecutionException e) {
            Assertions.fail("Error happened while running", e.getCause());
        }

    }
}
