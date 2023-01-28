package test.commands.parsers.mock;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl;
import org.jetbrains.annotations.NotNull;
import test.commands.parsers.EventEmitter;

import java.util.function.Consumer;

public class TextMessageAction extends MessageCreateActionImpl {

    private final EventEmitter publisher;
    private final @NotNull CharSequence text;

    public TextMessageAction(MessageChannel channel, EventEmitter publisher, @NotNull CharSequence text) {
        super(channel);
        this.publisher = publisher;
        this.text = text;
    }

    @Override
    public void queue(Consumer<? super Message> success, Consumer<? super Throwable> failure) {
        publisher.publishEvent(new EventEmitter.SendText(text));

    }
}
