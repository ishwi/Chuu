package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.TopEntity;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.GenreParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;

import java.util.List;

public class GenreParser extends DaoParser<GenreParameters> {
    private final ConcurrentLastFM lastFM;

    public GenreParser(ChuuService service, ConcurrentLastFM lastFM) {
        super(service);
        this.lastFM = lastFM;
    }

    @Override
    protected GenreParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        User sample = e.getAuthor();
        String genre;
        NowPlayingArtist nowPlayingInfo = null;
        boolean autoDetected = false;
        if (words.length == 0) {
            LastFMData lastfmFromID = findLastfmFromID(sample, e);
            nowPlayingInfo = lastFM.getNowPlayingInfo(lastfmFromID.getName());
            List<String> tags = lastFM.getTrackTags(1, TopEntity.TRACK, nowPlayingInfo.getArtistName(), nowPlayingInfo.getSongName());
            if (tags.isEmpty()) {
                tags = lastFM.getTrackTags(1, TopEntity.ALBUM, nowPlayingInfo.getArtistName(), nowPlayingInfo.getAlbumName());
            }
            if (tags.isEmpty()) {
                tags = lastFM.getTrackTags(1, TopEntity.ARTIST, nowPlayingInfo.getArtistName(), null);
            }
            if (tags.isEmpty()) {
                sendError("Was not able to find any tags on your now playing song/album/artist: "
                                + String.format("%s - %s | %s", nowPlayingInfo.getArtistName(), nowPlayingInfo.getSongName(), nowPlayingInfo.getAlbumName())
                        , e);
                return null;
            }
            autoDetected = true;
            genre = tags.get(0);
        } else genre = String.join(" ", words);
        return new GenreParameters(e, WordUtils.capitalizeFully(genre), autoDetected, nowPlayingInfo);


    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *genre* *username*** \n" +
                "\tIf username is not specified defaults to authors account \n" +
                "\tA genre can be specified or otherwise it defaults to the genre of your current track\\album\\artist according to last.fm\n";
    }
}
