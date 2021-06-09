package core.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public final record ContextMessageReceived(MessageReceivedEvent e) implements Context {
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
    public MessageChannel getChannel() {
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
        return e.getMessage().getContentRaw().charAt(0);
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
        return e.getChannel().sendMessage(embedBuilder.build());
    }

    @Override
    public List<User> getMentionedUsers() {
        return e.getMessage().getMentionedUsers();
    }

    @Override
    public RestAction<Message> sendMessage(String content) {
        return e.getChannel().sendMessage(content);
    }


    @Override
    public RestAction<Message> sendMessage(MessageEmbed embed) {
        return e.getChannel().sendMessage(embed);
    }

    @Override
    public RestAction<Message> sendMessage(MessageEmbed embed, List<ActionRow> rows) {
        return e.getChannel().sendMessage(embed).setActionRows(rows);
    }

    @Override
    public RestAction<Message> editMessage(@NotNull Message message, MessageEmbed embed, List<ActionRow> rows) {
        MessageAction action;
        if (embed != null) {
            action = message.editMessage(embed);
        } else {
            action = message.editMessage(message);
        }
        if (rows != null) {
            action = action.setActionRows(rows);
        }
        return action;
    }

    @Override
    public RestAction<Message> sendMessage(Message message, User toMention) {
        return e.getChannel().sendMessage(message).mentionUsers(toMention.getId());
    }

    @Override
    public void doSendImage(byte[] img, String format, EmbedBuilder embedBuilder) {
        MessageAction messageAction = e.getChannel().sendFile(img, "cat." + format);
        if (embedBuilder != null) {
            messageAction = messageAction.embed(embedBuilder.build());
        }
        messageAction.queue();
    }

    @Override
    public MessageAction sendFile(InputStream inputStream, String s, String title) {
        return e.getChannel().sendFile(inputStream, s).append(title);
    }
}
