package test.commands.parsers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.MessageMentionsImpl;
import net.dv8tion.jda.internal.entities.mentions.AbstractMentions;
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import org.apache.commons.collections4.map.ReferenceIdentityMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;
import test.commands.parsers.factories.Factory;
import test.commands.parsers.factories.FactoryDeps;

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
            public GuildMessageChannelUnion getGuildChannel() {
                return (GuildMessageChannelUnion) chann;
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

            @Override
            public MessageChannelUnion getChannel() {
                return (MessageChannelUnion) Objects.requireNonNull(chann);
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
            public @NotNull MessageCreateAction sendStickers(@NotNull StickerSnowflake... stickers) {
                return super.sendStickers(stickers);
            }


            @NotNull
            @Override
            public MessageCreateAction sendFiles(@NotNull FileUpload... files) {
                List<MessageCreateAction> actions = new ArrayList<>();
                for (FileUpload file : files) {
                    actions.add(new MessageCreateActionImpl(this));
                }
                return new MessageCreateActionImpl(this) {
                    @Override
                    public void queue(Consumer<? super Message> success, Consumer<? super Throwable> failure) {
                        for (FileUpload file : files) {
                            publisher.publishEvent(new EventEmitter.SendImage(file.getData(), file.getName()));

                        }
                    }
                }.addFiles(files);

            }


            @Override
            public @NotNull ChannelType getType() {
                return ChannelType.TEXT;
            }
        };
    }


}

