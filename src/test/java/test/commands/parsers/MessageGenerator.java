package test.commands.parsers;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.MessageMentionsImpl;
import net.dv8tion.jda.internal.entities.mentions.AbstractMentions;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import org.apache.commons.collections4.map.ReferenceIdentityMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;
import test.commands.parsers.factories.Factory;
import test.commands.parsers.factories.FactoryDeps;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class MessageGenerator {

    private final EventEmitter publisher;
    private final JDAImpl jda;
    private final Message message;
    private final GuildImpl guild;
    private final MessageReceivedEvent event;
    private User user;
    private Factory factory;

    public MessageGenerator(String content) {
        this(content, Factory.def());
    }

    public MessageGenerator(String content, Factory factory) {
        this.factory = factory;
        jda = mockJDA();

        FactoryDeps t = new FactoryDeps(jda, null);
        user = factory.userFn().apply(t);
        t = new FactoryDeps(jda, user);
        guild = factory.guildFn().apply(t);

        message = mockMessage(content);
        publisher = new EventEmitter(new HashMap<>(), new HashMap<>(), new ReferenceIdentityMap<>(), new AtomicInteger(0));
        event = mockMessageReceivedEvent(content);
    }

    private static JDAImpl mockJDA() {
        return new JDAImpl(new AuthorizationConfig("false-token"), null, null, null) {

        };


    }


    public GenericEvent event() {
        return event;
    }

    public EventEmitter publisher() {
        return publisher;
    }

    private MessageReceivedEvent mockMessageReceivedEvent(String content) {
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

            @Nullable
            @Override
            public Member getMember() {
                return new MemberImpl(guild, user);
            }

            @NotNull
            @Override
            public User getAuthor() {
                return user;
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

    private Message mockMessage(String content) {
        AbstractMentions abstractMentions = new MessageMentionsImpl(jda, guild, content, false, DataArray.empty(), DataArray.empty());
        return Mockito.mock(Message.class, invocation ->
                switch (invocation.getMethod().getName()) {
                    case "getmentionedmembers", "getmentionedusers" -> new ArrayList<>();
                    case "getContentRaw" -> content;
                    case "getCacheFlags" -> EnumSet.noneOf(CacheFlag.class);
                    case "getMentions" -> abstractMentions;
                    default -> null;
                });
    }

    private MessageChannel mockChannel() {
        return new MockedMessageChannel(publisher, jda, guild) {

            @Override
            public @NotNull MessageAction sendStickers(@NotNull Collection<? extends StickerSnowflake> stickers) {
                return super.sendStickers(stickers);
            }

            @NotNull
            @Override
            public MessageAction sendFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options) {
                return new MessageActionImpl(jda, null, this) {
                    @Override
                    public void queue(Consumer<? super Message> success, Consumer<? super Throwable> failure) {
                        publisher.publishEvent(new EventEmitter.SendImage(data, fileName));
                    }
                };
            }

            @Override
            public @NotNull ChannelType getType() {
                return ChannelType.TEXT;
            }
        };
    }


}

