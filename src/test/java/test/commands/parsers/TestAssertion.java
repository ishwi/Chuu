package test.commands.parsers;

import java.util.function.Consumer;

public record TestAssertion<T extends EventEmitter.TestEvent>(Class<T> receiver,
                                                              Consumer<T> assertion,
                                                              String as
) {
    public static TestAssertion<EventEmitter.SendedTyping> typing() {
        return new TestAssertion<>(
                EventEmitter.SendedTyping.class,
                (e) -> {
                },
                "Should first receive a Sending Typing event"
        );
    }

    public static TestAssertion<EventEmitter.SendImage> image(ChuuConsumer<EventEmitter.SendImage> assertion) {
        return image(assertion, "An image is expected");
    }

    public static TestAssertion<EventEmitter.SendImage> image(ChuuConsumer<EventEmitter.SendImage> assertion, String as) {
        return new TestAssertion<>(EventEmitter.SendImage.class, assertion, as);
    }

    public static TestAssertion<EventEmitter.SendText> text(Consumer<EventEmitter.SendText> assertion) {
        return text(assertion, "An image is expected");
    }

    public static TestAssertion<EventEmitter.SendText> text(Consumer<EventEmitter.SendText> assertion, String as) {
        return new TestAssertion<>(EventEmitter.SendText.class, assertion, as);
    }

    public static TestAssertion<EventEmitter.SendText> error(Consumer<CharSequence> assertion) {
        Consumer<EventEmitter.SendText> text = (txt) -> txt.error(assertion);
        return new TestAssertion<>(EventEmitter.SendText.class, text, "Error expected");
    }

}
