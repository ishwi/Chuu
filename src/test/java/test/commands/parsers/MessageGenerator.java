package test.commands.parsers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import org.apache.commons.collections4.Bag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.List;

public class MessageGenerator {

    public MessageReceivedEvent generateMessage(String content) {
        Message message = new Message() {
            @Override
            public @Nonnull List<User> getMentionedUsers() {
                return null;
            }

            @Override
            public @Nonnull Bag<User> getMentionedUsersBag() {
                return null;
            }

            @Override
            public @Nonnull List<TextChannel> getMentionedChannels() {
                return null;
            }

            @Override
            public @Nonnull Bag<TextChannel> getMentionedChannelsBag() {
                return null;
            }

            @Override
            public @Nonnull List<Role> getMentionedRoles() {
                return null;
            }

            @Override
            public @Nonnull Bag<Role> getMentionedRolesBag() {
                return null;
            }

            @Override
            public @Nonnull List<Member> getMentionedMembers(@Nonnull Guild guild) {
                return null;
            }

            @Override
            public @Nonnull List<Member> getMentionedMembers() {
                return null;
            }

            @Override
            public @Nonnull List<IMentionable> getMentions(@Nonnull MentionType... types) {
                return null;
            }

            @Override
            public boolean isMentioned(@Nonnull IMentionable mentionable, @Nonnull MentionType... types) {
                return false;
            }

            @Override
            public boolean mentionsEveryone() {
                return false;
            }

            @Override
            public boolean isEdited() {
                return false;
            }

            @Override
            public @Nullable OffsetDateTime getTimeEdited() {
                return null;
            }

            @Override
            public @Nonnull User getAuthor() {
                return null;
            }

            @Override
            public @Nullable Member getMember() {
                return null;
            }

            @Override
            public @Nonnull String getJumpUrl() {
                return null;
            }

            @Override
            public @Nonnull String getContentDisplay() {
                return null;
            }

            @Override
            public @Nonnull String getContentRaw() {
                return content;
            }

            @Override
            public @Nonnull String getContentStripped() {
                return null;
            }

            @Override
            public @Nonnull List<String> getInvites() {
                return null;
            }

            @Override
            public @Nullable String getNonce() {
                return null;
            }

            @Override
            public boolean isFromType(@Nonnull ChannelType type) {
                return false;
            }

            @Override
            public @Nonnull ChannelType getChannelType() {
                return null;
            }

            @Override
            public boolean isWebhookMessage() {
                return false;
            }

            @Override
            public @Nonnull MessageChannel getChannel() {
                return null;
            }

            @Override
            public @Nonnull PrivateChannel getPrivateChannel() {
                return null;
            }

            @Override
            public @Nonnull TextChannel getTextChannel() {
                return null;
            }

            @Override
            public @Nullable Category getCategory() {
                return null;
            }

            @Override
            public @Nonnull Guild getGuild() {
                return null;
            }

            @Override
            public @Nonnull List<Attachment> getAttachments() {
                return null;
            }

            @Override
            public @Nonnull List<MessageEmbed> getEmbeds() {
                return null;
            }

            @Override
            public @Nonnull List<Emote> getEmotes() {
                return null;
            }

            @Override
            public @Nonnull Bag<Emote> getEmotesBag() {
                return null;
            }

            @Override
            public @Nonnull List<MessageReaction> getReactions() {
                return null;
            }

            @Override
            public boolean isTTS() {
                return false;
            }

            @Override
            public @Nullable MessageActivity getActivity() {
                return null;
            }

            @Override
            public @Nonnull MessageAction editMessage(@Nonnull CharSequence newContent) {
                return null;
            }

            @Override
            public @Nonnull MessageAction editMessage(@Nonnull MessageEmbed newContent) {
                return null;
            }

            @Override
            public @Nonnull MessageAction editMessageFormat(@Nonnull String format, @Nonnull Object... args) {
                return null;
            }

            @Override
            public @Nonnull MessageAction editMessage(@Nonnull Message newContent) {
                return null;
            }

            @Override
            public @Nonnull AuditableRestAction<Void> delete() {
                return null;
            }

            @Override
            public @Nonnull JDA getJDA() {
                return null;
            }

            @Override
            public boolean isPinned() {
                return false;
            }

            @Override
            public @Nonnull RestAction<Void> pin() {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> unpin() {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> addReaction(@Nonnull Emote emote) {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> addReaction(@Nonnull String unicode) {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> clearReactions() {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> clearReactions(@Nonnull String unicode) {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> clearReactions(@Nonnull Emote emote) {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> removeReaction(@Nonnull Emote emote) {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> removeReaction(@Nonnull Emote emote, @Nonnull User user) {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> removeReaction(@Nonnull String unicode) {
                return null;
            }

            @Override
            public @Nonnull RestAction<Void> removeReaction(@Nonnull String unicode, @Nonnull User user) {
                return null;
            }

            @Override
            public @Nonnull ReactionPaginationAction retrieveReactionUsers(@Nonnull Emote emote) {
                return null;
            }

            @Override
            public @Nonnull ReactionPaginationAction retrieveReactionUsers(@Nonnull String unicode) {
                return null;
            }

            @Override
            public MessageReaction.@Nullable ReactionEmote getReactionByUnicode(@Nonnull String unicode) {
                return null;
            }

            @Override
            public MessageReaction.@Nullable ReactionEmote getReactionById(@Nonnull String id) {
                return null;
            }

            @Override
            public MessageReaction.@Nullable ReactionEmote getReactionById(long id) {
                return null;
            }

            @Override
            public @Nonnull AuditableRestAction<Void> suppressEmbeds(boolean suppressed) {
                return null;
            }

            @Override
            public @Nonnull RestAction<Message> crosspost() {
                return null;
            }

            @Override
            public boolean isSuppressedEmbeds() {
                return false;
            }

            @Override
            public @Nonnull EnumSet<MessageFlag> getFlags() {
                return null;
            }

            @Override
            public @Nonnull MessageType getType() {
                return null;
            }

            @Override
            public void formatTo(Formatter formatter, int flags, int width, int precision) {

            }

            @Override
            public long getIdLong() {
                return 0;
            }
        };
        return new MessageReceivedEvent(null, 0, message) {
        };

    }
}
