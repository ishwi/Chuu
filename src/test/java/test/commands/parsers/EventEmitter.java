package test.commands.parsers;


import org.apache.commons.collections4.map.ReferenceIdentityMap;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public record EventEmitter(Map<Class<? extends TestEvent>, BlockingQueue<? extends TestEvent>> queue,
                           Map<Class<? extends TestEvent>, CountDownLatch> latches,
                           ReferenceIdentityMap<TestEvent, Integer> positions,
                           AtomicInteger counter
) {


    public static final boolean JDWP = ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(s -> s.contains("jdwp"));

    @SuppressWarnings("unchecked")
    public <T extends TestEvent> void publishEvent(T event) {

        BlockingQueue<T> q = (BlockingQueue<T>) queue.computeIfAbsent(event.getClass(), (a) -> new ArrayBlockingQueue<>(10));
        q.add(event);
        positions.put(event, counter.getAndIncrement());
        CountDownLatch latch = latches.get(event.getClass());
        if (latch != null) {
            latch.countDown();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends TestEvent> T retrieveFirst(TestAssertion<T> assertion) {
        Class<T> receiver = assertion.receiver();
        BlockingQueue<? extends TestEvent> q = queue.get(receiver);
        if (q == null) {
            CountDownLatch latch = new CountDownLatch(1);
            latches.put(receiver, latch);
            try {
                boolean succesfully = latch.await(getSeconds(), TimeUnit.SECONDS);
                assertThat(succesfully).as("Awaited and received and event").isTrue();
                latches.remove(receiver);
                q = queue.get(receiver);
                assertThat(q).as("Now we should have at least one item").isNotNull();
                return (T) q.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return (T) q.poll(getSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends TestEvent> void runTest(TestAssertion<T> assertion, int index, AssertionMode mode) {
        T event = retrieveFirst(assertion);
        Integer eventPosition = positions.get(event);
        switch (mode) {
            case ORDER_REQUIRED -> assertThat(eventPosition)
                    .as("We are expecting the assertion %s in position %d", assertion, index)
                    .isEqualTo(index);
            case ANY_ORDER -> {
            }
        }
        assertThat(event).as(assertion.as()).isOfAnyClassIn(assertion.receiver());
        assertion.assertion().accept(event);
    }

    int getSeconds() {
        if (JDWP) {
            return 100;
        }
        return 10;
    }

    public sealed interface TestEvent {

    }

    public record SendedTyping() implements TestEvent {

    }

    public record SendImage(InputStream io, String filename) implements TestEvent {


    }

    public record SendText(CharSequence output) implements TestEvent {

        public void error(Consumer<CharSequence> errorMessage) {
            String message = "Error on testing's request:\n";
            assertThat(output).startsWith(message);
            errorMessage.accept(output.subSequence(message.length(), output.length()));
        }

    }

}
