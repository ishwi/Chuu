package test.commands.parsers;

import core.commands.Context;
import core.commands.ContextMessageReceived;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;


public class MessageGenerator {

    private final BlockingQueue<Object> sender;
    private final JDAImpl jda;
    private final Message message;
    private final MessageReceivedEvent event;

    public MessageGenerator(String content) {
        jda = mockJDA();
        message = mockMessage(jda, content);
        event = mockMessageReceivedEvent(jda, message, content);
        sender = new ArrayBlockingQueue<>(10);
    }

    private static JDAImpl mockJDA() {
        return Mockito.mock(JDAImpl.class, invocation ->
                switch (invocation.getMethod().getName()) {
                    case "getCacheFlags" -> EnumSet.noneOf(CacheFlag.class);
                    default -> null;
                }
        );
    }


    public Tuple build() {
        return new Tuple(sender, new ContextMessageReceived(event));
    }

    private MessageReceivedEvent mockMessageReceivedEvent(JDAImpl jda, Message message, String content) {
        MessageChannel chann = mockChannel();
        return new MessageReceivedEvent(jda, 0, message) {
            @NotNull
            @Override
            public GuildMessageChannel getGuildChannel() {
                return (GuildMessageChannel) chann;
            }

            @NotNull
            @Override
            public Guild getGuild() {
                return getGuildChannel().getGuild();
            }

            @NotNull
            @Override
            public User getAuthor() {
                return new UserImpl(0, jda) {
                    @Override
                    public boolean isBot() {
                        return false;
                    }
                };
            }

            @NotNull
            @Override
            public MessageChannel getChannel() {
                return Objects.requireNonNull(chann);
            }

            @Override
            public boolean isFromGuild() {
                return true;
            }
        };
    }

    private Message mockMessage(JDA jda, String content) {
        return Mockito.mock(Message.class, invocation ->
                switch (invocation.getMethod().getName()) {
                    case "getmentionedmembers", "getmentionedusers" -> new ArrayList<>();
                    case "getContentRaw" -> content;
                    case "getCacheFlags" -> EnumSet.noneOf(CacheFlag.class);
                    default -> null;
                });
    }

    private MessageChannel mockChannel() {
        return new MockedMessageChannel(jda) {
            @NotNull
            @Override
            public MessageAction sendFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options) {
                return new MessageActionImpl(jda, null, this) {
                    @Override
                    public void queue(Consumer<? super Message> success, Consumer<? super Throwable> failure) {
                        sender.add(data);
                    }
                };
            }

        };
    }

    public record Tuple(BlockingQueue<Object> receiver, Context result) {

    }

}

