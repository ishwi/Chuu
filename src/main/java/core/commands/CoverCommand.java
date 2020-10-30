package core.commands;

import core.exceptions.LastFmException;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CoverCommand extends AlbumPlaysCommand {

    public CoverCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public String getDescription() {
        return "The image of an album";
    }

    @Override
    public List<String> getAliases() {
        return List.of("cover", "co");
    }

    @Override
    public String getName() {
        return "Album Cover";
    }

    @Override
    void doSomethingWithAlbumArtist(ScrobbledArtist artist, String album, MessageReceivedEvent e, long who, ArtistAlbumParameters params) throws LastFmException {
        String albumUrl = lastFM.getAlbumSummary(params.getLastFMData().getName(), artist.getArtist(), album).getAlbumUrl();
        if (albumUrl == null || albumUrl.isBlank()) {
            sendMessageQueue(e,
                    String.format("There is no image for %s on last.fm at the moment.\nConsider submitting one: https://www.last.fm/music/%s/%s/+images/upload"
                            , album, URLEncoder.encode(artist.getArtist(), StandardCharsets.UTF_8), URLEncoder.encode(album, StandardCharsets.UTF_8)));
            return;
        }
        try {
            albumUrl = albumUrl.replaceAll("i/u/[\\d\\w]+/", "i/u/4096x4096/");
            InputStream file = new URL(albumUrl).openStream();
            e.getChannel().sendFile(file, "cat.png").append(String.format("**%s** - **%s**", artist.getArtist(), album)).queue();
        } catch (IOException ioException) {
            sendMessageQueue(e, "An error occurred while sending the album cover for " + artist.getArtist() + " - " + album);
        }

    }
}
