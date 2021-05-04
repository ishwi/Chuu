package core.commands.loved;

import core.apis.last.entities.Scrobble;
import core.commands.Context;
import core.commands.albums.AlbumPlaysCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;

import java.util.List;

public class LoveCommand extends AlbumPlaysCommand {
    public LoveCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.LOVE;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistSongParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Loves a song on last.fm";
    }

    @Override
    public List<String> getAliases() {
        return List.of("love");
    }

    @Override
    public String getName() {
        return "Love";
    }

    protected void doSomethingWithAlbumArtist(ScrobbledArtist artist, String song, Context e, long who, ArtistAlbumParameters params) throws LastFmException {
        LastFMData user = params.getLastFMData();
        if (user.getSession() == null || user.getSession().isBlank()) {
            sendMessageQueue(e, "Only users that have linked their account via `%slogin` can love/unlove songs!".formatted(CommandUtil.getMessagePrefix(e)));
            return;
        }
        lastFM.love(user.getSession(), new Scrobble(artist.getArtist(), null, song, null, null));

        sendMessageQueue(e, "Successfully loved **%s** by **%s**.".formatted(song, artist.getArtist()));
    }
}
