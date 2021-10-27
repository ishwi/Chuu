package core.otherlisteners;

import core.Chuu;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.time.Year;

import static core.otherlisteners.Reactions.ACCEPT;
import static core.otherlisteners.Reactions.REJECT;

public final class AlbumYearApproval extends ChannelConstantListener {
    private final ChuuService service;

    public AlbumYearApproval(long channelId, ChuuService service) {
        super(channelId);
        this.service = service;
    }

    @Override
    public void handleClick(ButtonClickEvent e) {
        Message message = e.getMessage();
        if (e.getComponentId().equals(REJECT)) {
            message.delete().queue();
        } else if (e.getComponentId().equals(ACCEPT)) {
            doSomething(message);
        }
    }


    private void doSomething(Message x) {
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
                        Chuu.getShardManager().retrieveUserById((author))
                ).flatMap(User::openPrivateChannel).flatMap(t ->
                        t.sendMessage(artist + " - " + album + " is now a " + year + " album")
                ).queue();
            }
        }
    }

}
