package test.commands.parsers.mock;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.jetbrains.annotations.NotNull;
import test.commands.parsers.EventEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MockedMessageChannel implements MessageChannelUnion, GuildMessageChannel, GuildMessageChannelUnion {

    private final EventEmitter eventEmitter;
    private final JDAImpl jda;
    private final GuildImpl guild;

    public MockedMessageChannel(
            EventEmitter eventEmitter, JDAImpl jda,
            GuildImpl guild) {
        this.eventEmitter = eventEmitter;
        this.jda = jda;
        this.guild = guild;
    }


    @Override
    public long getIdLong() {
        return -1;
    }

    @NotNull
    @Override
    public RestAction<Void> sendTyping() {
        eventEmitter.publishEvent(new EventEmitter.SendedTyping());
        return new CompletedRestAction<>(jda, null);
    }

    @Override
    public MessageCreateAction sendMessage(MessageCreateData msg) {
        List<MessageCreateAction> messageCreateAction = new ArrayList<>();
        if (!msg.getFiles().isEmpty()) {
            messageCreateAction.add(sendFiles(msg.getFiles().toArray(FileUpload[]::new)));
        }
        if (!msg.getContent().isBlank()) {
            messageCreateAction.add(sendMessage(msg.getContent()));
        }
        if (!msg.getEmbeds().isEmpty()) {
            messageCreateAction.add(sendMessageEmbeds(msg.getEmbeds()));
        }
        return new MessageCreateActionCompose(messageCreateAction, this);

    }

    @NotNull
    @Override
    public MessageCreateAction sendFiles(@NotNull FileUpload... files) {
        return new CustomFileMessageAction(this, eventEmitter, Arrays.asList(files)).addFiles(files);
    }

    @NotNull
    @Override
    public MessageCreateAction sendMessage(@NotNull CharSequence text) {
        return new TextMessageAction(this, eventEmitter, text);
    }

    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return ChannelType.TEXT;
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
    public MessageCreateAction sendStickers(@NotNull Collection<? extends StickerSnowflake> stickers) {
        return null;
    }


    @Override
    public int compareTo(@NotNull GuildChannel o) {
        return 0;
    }

    @Override
    public PrivateChannel asPrivateChannel() {
        return null;
    }

    @Override
    public TextChannel asTextChannel() {
        return (TextChannel) this;
    }

    @Override
    public NewsChannel asNewsChannel() {
        return null;
    }

    @Override
    public ThreadChannel asThreadChannel() {
        return null;
    }

    @Override
    public VoiceChannel asVoiceChannel() {
        return null;
    }

    @Override
    public StageChannel asStageChannel() {
        return null;
    }

    @Override
    public IThreadContainer asThreadContainer() {
        return null;
    }

    @Override
    public GuildMessageChannel asGuildMessageChannel() {
        return null;
    }

    @Override
    public AudioChannel asAudioChannel() {
        return null;
    }

    @Override
    public StandardGuildChannel asStandardGuildChannel() {
        return null;
    }

    @Override
    public StandardGuildMessageChannel asStandardGuildMessageChannel() {
        return null;
    }
}
