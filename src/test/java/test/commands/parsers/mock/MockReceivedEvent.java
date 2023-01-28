package test.commands.parsers.mock;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MockReceivedEvent extends MessageReceivedEvent {
    private final MessageChannel chann;
    private final GuildImpl guild;
    private final User user;

    public MockReceivedEvent(JDAImpl jda, MessageChannel chann, Message message, GuildImpl guild, User user) {
        super(jda, 0, message);
        this.chann = chann;
        this.guild = guild;
        this.user = user;
    }

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
}
