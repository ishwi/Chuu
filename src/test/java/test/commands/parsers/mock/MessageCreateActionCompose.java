package test.commands.parsers.mock;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl;

import java.util.List;
import java.util.function.Consumer;

public class MessageCreateActionCompose extends MessageCreateActionImpl {
    private final List<MessageCreateAction> actions;

    public MessageCreateActionCompose(List<MessageCreateAction> actions, MessageChannel channel) {
        super(channel);
        this.actions = actions;
    }

    @Override
    public void queue(Consumer<? super Message> success, Consumer<? super Throwable> failure) {
        actions.forEach(a -> a.queue(success, failure));
    }
}
