package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.Track;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class TrackPlaysCommand extends AlbumPlaysCommand {
    public TrackPlaysCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> getParser() {
        return new ArtistSongParser(getService(), lastFM);
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
    void doSomethingWithAlbumArtist(ScrobbledArtist artist, String song, MessageReceivedEvent e, long who, ArtistAlbumParameters params) throws InstanceNotFoundException, LastFmException {
        LastFMData lastFMData = params.getLastFMData();

        Track trackInfo = lastFM.getTrackInfo(lastFMData.getName(), artist.getArtist(), song);
        String usernameString = getUserString(e, who, lastFMData.getName());

        int plays = trackInfo.getPlays();
        String ending = plays == 1 ? "time " : "times";

        sendMessageQueue(e, String.format("**%s** has listened **%s** %d %s", usernameString, CommandUtil.cleanMarkdownCharacter(song), plays, ending));
    }
}
