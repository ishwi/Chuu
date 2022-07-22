package test.commands.parsers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MockedMessageChannel implements MessageChannel, GuildMessageChannel {
    private JDAImpl jda;
    private GuildImpl guild;

    public MockedMessageChannel(JDAImpl jda) {
        this.jda = jda;
        guild = new GuildImpl(jda, -1);
    }

    @Override
    public long getIdLong() {
        return -1;
    }

    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return null;
    }

    @NotNull
    @Override
    public JDA getJDA() {
        return jda;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return guild;
    }

    @NotNull
    @Override
    public ChannelManager<?, ?> getManager() {
        return null;
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> delete() {
        return null;
    }


    @Override
    public IPermissionContainer getPermissionContainer() {
        return null;
    }

    @Override
    public long getLatestMessageIdLong() {
        return 0;
    }

    @Override
    public boolean canTalk() {
        return false;
    }

    @Override
    public boolean canTalk(@NotNull Member member) {
        return false;
    }

    @NotNull
    @Override
    public RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull Emoji emoji, @NotNull User user) {
        return null;
    }

    @NotNull
    @Override
    public RestAction<Void> deleteMessagesByIds(@NotNull Collection<String> messageIds) {
        return null;
    }

    @NotNull
    @Override
    public RestAction<Void> clearReactionsById(@NotNull String messageId) {
        return null;
    }

    @NotNull
    @Override
    public RestAction<Void> clearReactionsById(@NotNull String messageId, @NotNull Emoji emoji) {
        return null;
    }

    @NotNull
    @Override
    public MessageAction sendStickers(@NotNull Collection<? extends StickerSnowflake> stickers) {
        return null;
    }

    @Override
    public int compareTo(@NotNull GuildChannel o) {
        return 0;
    }
}
