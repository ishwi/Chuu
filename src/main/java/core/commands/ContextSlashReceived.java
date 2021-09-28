package core.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

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
    public String toLog() {
        return "%s -> /%s | %s".formatted(e.getUser(), e.getCommandPath(), e.getOptions().stream().map(OptionMapping::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public long getId() {
        return e.getIdLong();
    }

    @Override
    public RestAction<Message> sendEmbed(EmbedBuilder embedBuilder) {
        return e.getHook().sendMessageEmbeds(embedBuilder.build());
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
        return e.getHook().sendMessageEmbeds(embed);
    }

    @Override
    public RestAction<Message> sendMessage(MessageEmbed embed, List<ActionRow> rows) {
        return e.getHook().sendMessageEmbeds(embed).addActionRows(rows);
    }

    @Override
    public RestAction<Message> editMessage(Message ignored, @Nullable MessageEmbed embed, @Nullable List<ActionRow> rows) {
        WebhookMessageUpdateAction<Message> action;
        if (embed != null) {
            action = e.getHook().editMessageEmbedsById("@original", embed).setActionRows(rows);
        } else {
            action = e.getHook().editOriginalComponents(rows);
        }
        return action;
    }

    @Override
    public RestAction<Message> sendMessage(Message message, User toMention) {
        return e.getHook().sendMessage(message).mention(toMention);
    }

    @Override
    public void doSendImage(byte[] img, String format, @Nullable EmbedBuilder embedBuilder) {
        WebhookMessageAction<Message> interactionWebhookAction = e.getHook().sendFile(img, "cat." + format);
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
