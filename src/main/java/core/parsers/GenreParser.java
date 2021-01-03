package core.parsers;

import core.apis.ExecutorsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.entities.chartentities.TopEntity;
import core.exceptions.LastFmException;
import core.parsers.params.GenreParameters;
import core.services.NPService;
import core.services.TagAlbumService;
import core.services.TagArtistService;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.ArtistInfo;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class GenreParser extends DaoParser<GenreParameters> {
    private final ConcurrentLastFM lastFM;
    private final ExecutorService executor;

    public GenreParser(ChuuService service, ConcurrentLastFM lastFM) {
        super(service);
        this.lastFM = lastFM;
        executor = ExecutorsSingleton.getInstance();
    }

    @Override
    protected GenreParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        User sample = e.getAuthor();
        String genre;
        NowPlayingArtist nowPlayingInfo = null;
        LastFMData lastFMData;
        boolean autoDetected = false;
        if (words.length == 0) {
            lastFMData = findLastfmFromID(sample, e);
            nowPlayingInfo = new NPService(lastFM, lastFMData).getNowPlaying();
            List<String> tags = lastFM.getTrackTags(1, TopEntity.TRACK, nowPlayingInfo.getArtistName(), nowPlayingInfo.getSongName());
            if (tags.isEmpty()) {
                tags = lastFM.getTrackTags(1, TopEntity.ALBUM, nowPlayingInfo.getArtistName(), nowPlayingInfo.getAlbumName());
            } else {
                executor.submit(new TagArtistService(dao, lastFM, tags, new ArtistInfo(nowPlayingInfo.getUrl(), nowPlayingInfo.getArtistName(), nowPlayingInfo.getArtistMbid())));
            }
            if (tags.isEmpty()) {
                tags = lastFM.getTrackTags(1, TopEntity.ARTIST, nowPlayingInfo.getArtistName(), null);
            } else {
                if (nowPlayingInfo.getAlbumName() != null && !nowPlayingInfo.getAlbumName().isBlank())
                    executor.submit(new TagAlbumService(dao, lastFM, tags, new AlbumInfo(nowPlayingInfo.getAlbumMbid(), nowPlayingInfo.getAlbumName(), nowPlayingInfo.getArtistName())));
            }
            if (tags.isEmpty()) {
                sendError("Was not able to find any tags on your now playing song/album/artist: "
                                + String.format("%s - %s | %s", nowPlayingInfo.getArtistName(), nowPlayingInfo.getSongName(), nowPlayingInfo.getAlbumName())
                        , e);
                return null;
            } else {
                executor.submit(new TagArtistService(dao, lastFM, tags, new ArtistInfo(nowPlayingInfo.getUrl(), nowPlayingInfo.getArtistName(), nowPlayingInfo.getArtistMbid())));
            }
            autoDetected = true;
            genre = tags.get(0);
        } else {
            genre = String.join(" ", words);
            lastFMData = null;
        }
        return new GenreParameters(e, WordUtils.capitalizeFully(genre), autoDetected, nowPlayingInfo, lastFMData, sample);

    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *genre* *username*** \n" +
                "\tIf username is not specified defaults to authors account \n" +
                "\tA genre can be specified or otherwise it defaults to the genre of your current track\\album\\artist according to last.fm\n";
    }
}
