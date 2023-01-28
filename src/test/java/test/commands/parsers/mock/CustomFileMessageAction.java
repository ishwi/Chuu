package test.commands.parsers.mock;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl;
import test.commands.parsers.EventEmitter;

import java.util.List;
import java.util.function.Consumer;

public class CustomFileMessageAction extends MessageCreateActionImpl {

    private final EventEmitter publisher;
    private final List<FileUpload> files;

    public CustomFileMessageAction(MessageChannel channel, EventEmitter publisher, List<FileUpload> files) {
        super(channel);
        this.publisher = publisher;
        this.files = files;
    }

    @Override
    public void queue(Consumer<? super Message> success, Consumer<? super Throwable> failure) {
        for (FileUpload file : files) {
            publisher.publishEvent(new EventEmitter.SendImage(file.getData(), file.getName()));
        }
    }
}
