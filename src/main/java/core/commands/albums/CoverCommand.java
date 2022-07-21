package core.commands.albums;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistAlbumParameters;
import core.util.ServiceView;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CoverCommand extends AlbumPlaysCommand {

    public CoverCommand(ServiceView dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
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
    protected void doSomethingWithAlbumArtist(ScrobbledArtist artist, String album, Context e, long who, ArtistAlbumParameters params) throws LastFmException {
        String albumUrl = lastFM.getAlbumSummary(params.getLastFMData(), artist.getArtist(), album).getAlbumUrl();
        albumUrl = Chuu.getCoverService().getCover(params.getArtist(), params.getAlbum(), albumUrl, e);
        if (albumUrl == null || albumUrl.isBlank()) {
            sendMessageQueue(e,
                    String.format("There is no image for %s on last.fm at the moment.\nConsider submitting one: https://www.last.fm/music/%s/%s/+images/upload"
                            , album, URLEncoder.encode(artist.getArtist(), StandardCharsets.UTF_8), URLEncoder.encode(album, StandardCharsets.UTF_8)));
            return;
        }
        String big = albumUrl.replaceAll("i/u/[\\d\\w]+/", "i/u/4096x4096/");
        InputStream io = null;
        try {
            io = new URL(big).openStream();
            String finalAlbumUrl = albumUrl;
            InputStream finalIo = io;
            sendCover(io, artist, album, e, big).queue(message -> closeIO(finalIo), throwable -> {
                closeIO(finalIo);
                InputStream io2 = null;
                try {
                    io2 = new URL(finalAlbumUrl).openStream();
                    InputStream finalIo1 = io2;
                    sendCover(io2, artist, album, e, finalAlbumUrl).queue(a -> closeIO(finalIo1), b -> {
                        closeIO(finalIo1);
                        sendError(e, artist.getArtist(), album);
                    });
                } catch (IOException exception) {
                    closeIO(io2);
                    sendError(e, artist.getArtist(), album);
                }
            });
        } catch (IOException exception) {
            closeIO(io);
            InputStream io2 = null;
            try {
                io2 = new URL(big).openStream();
                InputStream finalIo = io2;
                sendCover(io2, artist, album, e, albumUrl).queue(message -> closeIO(finalIo), throwable -> sendError(e, artist.getArtist(), album));
            } catch (IOException e2) {
                closeIO(io2);
                sendError(e, artist.getArtist(), album);
            }
        }

    }

    private void closeIO(InputStream io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException exception) {
                Chuu.getLogger().warn(exception.getMessage(), exception);
            }
        }
    }

    private void sendError(Context e, String artist, String album) {
        sendMessageQueue(e, "An error occurred while sending the album cover for " + artist + " - " + album);

    }

    @CheckReturnValue
    private RestAction<Message> sendCover(InputStream inputStream, ScrobbledArtist artist, String album, Context e, String albumUrl) throws IOException {
        return e.sendFile(inputStream, "cat.png", String.format("**%s** - **%s**", artist.getArtist(), album));
    }
}
