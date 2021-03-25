package core.otherlisteners;

import dao.ChuuService;
import dao.entities.AlbumInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;
import java.time.Year;
import java.util.List;

public record ConstantListener(long channelId, ChuuService service) implements EventListener {
    private static final String ACCEPT = "U+2714";
    private static final String REJECT = "U+274c";

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof MessageReactionAddEvent e) {
            onMessageReactionAdd(e);
        }
    }

    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        long idLong = e.getChannel().getIdLong();
        if (idLong != channelId || e.getUser() == null || e.getUser().isBot()) {
            return;
        }
        if (e.getReaction().getReactionEmote().getAsCodepoints().equals(REJECT)) {
            return;
        }
        e.getChannel().retrieveMessageById(e.getMessageId()).queue(x -> {
            if (e.getReaction().getReactionEmote().getAsCodepoints().equals(REJECT)) {
                x.delete().queue();
            } else if (e.getReaction().getReactionEmote().getAsCodepoints().equals(ACCEPT)) {
                String description = x.getEmbeds().get(0).getDescription();
                if (description != null) {
                    String[] split = description.split("\n");
                    if (split.length == 4) {
                        String artist = split[0].split("Artist: ")[1].replaceAll("\\*\\*", "").trim();
                        String album = split[1].split("Album: ")[1].replaceAll("\\*\\*", "").trim();
                        String year = split[2].split("Year: ")[1].replaceAll("\\*\\*", "").trim();
                        String author = split[3].split("Author: ")[1].replaceAll("\\*\\*", "").trim();
                        service.insertAlbumsOfYear(List.of(new AlbumInfo(album, artist)), Year.parse(year));
                        x.delete().flatMap(b ->
                                core.Chuu.getShardManager().retrieveUserById((author))
                        ).flatMap(User::openPrivateChannel).flatMap(t ->
                                t.sendMessage(artist + " - " + album + " is now a " + year + " album")
                        ).queue();
                    }
                }
            }
        });
    }
}
