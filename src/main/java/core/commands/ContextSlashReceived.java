package core.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.InteractionWebhookAction;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.List;

public final record ContextSlashReceived(SlashCommandEvent e) implements Context {
    @Override
    public User getAuthor() {
        return e.getUser();
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
        return '/';
    }

    @Override
    public long getId() {
        return e.getIdLong();
    }

    @Override
    public RestAction<Message> sendEmbed(EmbedBuilder embedBuilder) {
        return e.getHook().sendMessage(embedBuilder.build());
    }

    @Override
    public List<User> getMentionedUsers() {
        return e.getOptions().stream().filter(t -> t.getType() == OptionType.USER).map(OptionMapping::getAsUser).toList();
    }

    @Override
    public RestAction<Message> sendMessage(String message) {
        return e.getHook().sendMessage(message);
    }


    @Override
    public RestAction<Message> sendMessage(MessageEmbed embed) {
        return e.getHook().sendMessage(embed);
    }

    @Override
    public RestAction<Message> sendMessage(MessageEmbed embed, User toMention) {
        return e.getHook().sendMessage(embed).mention(toMention);
    }

    @Override
    public void doSendImage(byte[] img, String format, @Nullable EmbedBuilder embedBuilder) {
        InteractionWebhookAction interactionWebhookAction = e.getHook().sendFile(img, "cat." + format);
        if (embedBuilder != null) {
            interactionWebhookAction = interactionWebhookAction.addEmbeds(embedBuilder.build());
        }
        interactionWebhookAction.queue();
    }

    @Override
    public RestAction<Message> sendFile(InputStream inputStream, String s, String title) {
        return e.getHook().sendFile(inputStream, s).setContent(title);
    }
}
