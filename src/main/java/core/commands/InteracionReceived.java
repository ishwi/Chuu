package core.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public sealed abstract class InteracionReceived<T extends CommandInteraction> implements Context permits ContextUserCommandReceived, ContextSlashReceived {
    protected final T e;

    public InteracionReceived(T e) {
        this.e = e;
    }

    public T e() {
        return e;
    }

    @Override
    public User getAuthor() {
        return e.getUser();
    }

    @Override
    public GenericEvent getEvent() {
        return null;
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
        return "%s -> /%s | %s".formatted(e.getUser(), e.getFullCommandName(), e.getOptions().stream().map(OptionMapping::toString).collect(Collectors.joining(", ")));
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
        return e.getHook().sendMessageEmbeds(embed).setComponents(rows);
    }

    @Override
    public RestAction<Message> editMessage(Message ignored, @Nullable MessageEmbed embed, @Nullable List<ActionRow> rows) {
        RestAction<Message> action;
        if (embed != null) {
            action = e.getHook().editMessageEmbedsById("@original", embed).setComponents(rows);
        } else {
            action = e.getHook().editOriginalComponents(rows);
        }
        return action;
    }

    @Override
    public RestAction<Message> sendMessage(MessageCreateData message, User toMention) {
        return e.getHook().sendMessage(message).mention(toMention);
    }

    @Override
    public void doSendImage(byte[] img, String format, @Nullable EmbedBuilder embedBuilder) {
        var interactionWebhookAction = e.getHook().sendFiles(FileUpload.fromData(img, "cat." + format));
        if (embedBuilder != null) {
            interactionWebhookAction = interactionWebhookAction.addEmbeds(embedBuilder.build());
        }
        interactionWebhookAction.queue();
    }

    @Override
    public RestAction<Message> sendFile(InputStream inputStream, String s, String title) {
        return e.getHook().sendFiles(FileUpload.fromData(inputStream, s)).setContent(title);
    }
}
