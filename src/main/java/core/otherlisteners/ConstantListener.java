package core.otherlisteners;

import dao.ChuuService;
import dao.entities.AlbumInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;
import java.time.Year;

import static core.otherlisteners.Reactions.ACCEPT;
import static core.otherlisteners.Reactions.REJECT;

public record ConstantListener(long channelId, ChuuService service) implements EventListener {

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof MessageReactionAddEvent e) {
            onMessageReactionAdd(e);
        }
        if (event instanceof ButtonClickEvent e) {
            e.deferEdit().queue();
            onButtonClicked(e);
        }
    }

    public void onButtonClicked(ButtonClickEvent e) {
        long idLong = e.getChannel().getIdLong();
        if (idLong != channelId || e.getUser() == null || e.getUser().isBot()) {
            return;
        }

        Message message = e.getMessage();
        if (e.getComponentId().equals(REJECT)) {
            message.delete().queue();
        } else if (e.getComponentId().equals(ACCEPT)) {
            a(message);
        }
    }

    private void a(Message x) {
        String description = x.getEmbeds().get(0).getDescription();
        if (description != null) {
            String[] split = description.split("\n");
            if (split.length == 4) {
                String artist = split[0].split("Artist: ")[1].replaceAll("\\*\\*", "").trim();
                String album = split[1].split("Album: ")[1].replaceAll("\\*\\*", "").trim();
                String year = split[2].split("Year: ")[1].replaceAll("\\*\\*", "").trim();
                String author = split[3].split("Author: ")[1].replaceAll("\\*\\*", "").replaceAll("[<>@!]", "").trim();
                service.insertAlbumOfYear(new AlbumInfo(album, artist), Year.parse(year));
                x.delete().flatMap(b ->
                        core.Chuu.getShardManager().retrieveUserById((author))
                ).flatMap(User::openPrivateChannel).flatMap(t ->
                        t.sendMessage(artist + " - " + album + " is now a " + year + " album")
                ).queue();
            }
        }
    }

    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        long idLong = e.getChannel().getIdLong();
        if (idLong != channelId || e.getUser() == null || e.getUser().isBot()) {
            return;
        }
        e.getChannel().retrieveMessageById(e.getMessageId()).queue(x -> {
            if (e.getReaction().getReactionEmote().getAsCodepoints().equals(REJECT)) {
                x.delete().queue();
            } else if (e.getReaction().getReactionEmote().getAsCodepoints().equals(ACCEPT)) {
                a(x);
            }
        });
    }
}
