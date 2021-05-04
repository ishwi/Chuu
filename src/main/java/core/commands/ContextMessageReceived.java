package core.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.io.InputStream;
import java.util.List;

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
    public RestAction<Message> sendMessage(Message message) {
        return e.getChannel().sendMessage(message);
    }

    @Override
    public RestAction<Message> sendMessage(MessageEmbed embed) {
        return e.getChannel().sendMessage(embed);
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
