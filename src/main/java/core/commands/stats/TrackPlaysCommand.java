package core.commands.stats;

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
import dao.entities.Track;

import java.util.List;

public class TrackPlaysCommand extends AlbumPlaysCommand {
    public TrackPlaysCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistSongParser(db, lastFM);
    }


    @Override
    public String getDescription() {
        return "Your Plays on a given song";
    }

    @Override
    public List<String> getAliases() {
        return List.of("track");
    }

    @Override
    public String getName() {
        return "Track Plays";
    }


    @Override
    protected void doSomethingWithAlbumArtist(ScrobbledArtist artist, String song, Context e, long who, ArtistAlbumParameters params) throws LastFmException {
        LastFMData lastFMData = params.getLastFMData();

        Track trackInfo = lastFM.getTrackInfo(lastFMData, artist.getArtist(), song);
        String usernameString = getUserString(e, who, lastFMData.getName());

        int plays = trackInfo.getPlays();
        String ending = plays == 1 ? "time " : "times";

        sendMessageQueue(e, String.format("**%s** has scrobbled **%s** %d %s", usernameString, CommandUtil.cleanMarkdownCharacter(song), plays, ending));
    }
}
