package test.commands.parsers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.SelfUserImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import org.apache.commons.collections4.map.ReferenceIdentityMap;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import test.commands.parsers.factories.Factory;
import test.commands.parsers.factories.FactoryDeps;
import test.commands.parsers.mock.CustomMentionMessage;
import test.commands.parsers.mock.MockReceivedEvent;
import test.commands.parsers.mock.MockedMessageChannel;
import test.commands.utils.TestResources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class MessageGenerator {

    private final EventEmitter publisher;
    private final JDAImpl jda;
    private final Message message;
    private final GuildImpl guild;
    private final MessageReceivedEvent event;
    private final User user;
    private final Factory factory;

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
        event = mockMessageReceivedEvent();
    }

    public static JDAImpl mockJDA() {
        return new JDAImpl(new AuthorizationConfig("false-token"), null, null, null, null) {
            @Override
            public SelfUser getSelfUser() {
                return new SelfUserImpl(-1L, null);
            }

            @NotNull
            @Override
            public List<Object> getRegisteredListeners() {
                return TestResources.manager.getRegisteredListeners();
            }
        };


    }


    public GenericEvent event() {
        return event;
    }

    public EventEmitter publisher() {
        return publisher;
    }

    private MessageReceivedEvent mockMessageReceivedEvent() {
        return new MockReceivedEvent(jda, mockChannel(), message, guild, user);
    }

    private Message mockMessage(String content) {
        return Mockito.mock(Message.class, invocation ->
                switch (invocation.getMethod().getName()) {
                    case "getmentionedmembers", "getmentionedusers" -> new ArrayList<>();
                    case "getContentRaw" -> content;
                    case "getCacheFlags" -> EnumSet.noneOf(CacheFlag.class);
                    case "getMentions" -> new CustomMentionMessage(jda, guild, content);
                    case "getAttachments" -> Collections.emptyList();
                    default -> null;
                });
    }

    private MessageChannelUnion mockChannel() {
        return new MockedMessageChannel(publisher, jda, guild);
    }


}

