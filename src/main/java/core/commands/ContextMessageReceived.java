package core.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public record ContextMessageReceived(MessageReceivedEvent e, boolean isPingPrefix) implements Context {
    @Override
    public User getAuthor() {
        return e.getAuthor();
    }

    @Override
    public GenericEvent getEvent() {
        return e;
    }

    @Override
    public JDA getJDA() {
        return e.getJDA();
    }

    @Override
    public Member getMember() {
        return e.getMember();
    }

    @Override
    public MessageChannelUnion getChannelUnion() {
        return e.getChannel();
    }

    @Override
    public Guild getGuild() {
        return e.getGuild();
    }

    @Override
    public boolean isFromGuild() {
        return e.isFromGuild();
    }

    @Override
    public char getPrefix() {
        return isPingPrefix() ? '@' : e.getMessage().getContentRaw().charAt(0);
    }

    @Override
    public String toLog() {
        return "%s -> %s | attachments -> %s".formatted(e.getAuthor().getIdLong(), e.getMessage().getContentRaw(),
                e.getMessage().getAttachments().stream().map(Message.Attachment::getUrl).collect(Collectors.joining(", ")));
    }

    @Override
    public long getId() {
        return e.getMessageIdLong();
    }

    @Override
    public RestAction<Message> sendEmbed(EmbedBuilder embedBuilder) {
        return e.getChannel().sendMessageEmbeds(embedBuilder.build());
    }

    @Override
    public List<User> getMentionedUsers() {
        return e.getMessage().getMentions().getUsers().stream().filter(e -> e.getIdLong() != e.getJDA().getSelfUser().getIdLong()).toList();
    }

    @Override
    public RestAction<Message> sendMessage(String content) {
        return e.getChannel().sendMessage(content);
    }


    @Override
    public RestAction<Message> sendMessage(MessageEmbed embed) {
        return e.getChannel().sendMessageEmbeds(embed);
    }

    @Override
    public RestAction<Message> sendMessage(MessageEmbed embed, List<ActionRow> rows) {
        return e.getChannel().sendMessageEmbeds(embed).setComponents(rows);
    }

    @Override
    public MessageEditAction editMessage(@Nullable Message message, MessageEmbed embed, @Nullable List<ActionRow> rows) {
        assert message != null;
        MessageEditAction action;
        if (embed != null) {
            action = message.editMessageEmbeds(embed);
        } else {
            action = message.editMessage(MessageEditData.fromMessage(message));
        }
        if (rows != null) {
            action = action.setComponents(rows);
        }

        return action;
    }

    @Override
    public RestAction<Message> sendMessage(MessageCreateData message, User toMention) {
        return e.getChannel().sendMessage(message).mentionUsers(toMention.getId());
    }

    @Override
    public void doSendImage(byte[] img, String format, EmbedBuilder embedBuilder) {
        MessageCreateAction messageAction = e.getChannel().sendMessage(MessageCreateData.fromFiles(FileUpload.fromData(img, "cat." + format)));
        if (embedBuilder != null) {
            messageAction = messageAction.setEmbeds(embedBuilder.build());
        }
        messageAction.queue();
    }

    @Override
    public RestAction<Message> sendFile(InputStream inputStream, String s, String title) {
        MessageCreateData messageCreateData = MessageCreateData.fromFiles(FileUpload.fromData(inputStream, s));
        return e.getChannel().sendMessage(messageCreateData).setContent(title);
    }


}
