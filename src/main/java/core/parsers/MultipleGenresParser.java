package core.parsers;

import core.apis.ExecutorsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.entities.chartentities.TopEntity;
import core.exceptions.LastFmException;
import core.parsers.params.MultipleGenresParameters;
import core.services.TagAlbumService;
import core.services.TagArtistService;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.ArtistInfo;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class MultipleGenresParser extends MultiStringParser<MultipleGenresParameters> {
    private final ConcurrentLastFM lastFM;
    private final ExecutorService executor;


    public MultipleGenresParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... opts) {
        super(dao, opts);
        this.lastFM = lastFM;
        executor = ExecutorsSingleton.getInstance();

    }

    @Override
    void setUpOptionals() {
        super.setUpOptionals();
        opts.add(new OptionalEntity("any", "return artist that match at least one tag"));
    }

    @Override
    protected MultipleGenresParameters doSomethingNoWords(int limit, LastFMData lastFMData, MessageReceivedEvent e) throws LastFmException {
        NowPlayingArtist nowPlayingInfo = lastFM.getNowPlayingInfo(lastFMData);
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
        return new MultipleGenresParameters(e, lastFMData, new HashSet<>(tags));
    }

    @Override
    protected MultipleGenresParameters doSomethingWords(LastFMData lastFMData, MessageReceivedEvent e, Set<String> strings) {
        return new MultipleGenresParameters(e, lastFMData, strings);

    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[genre genre1 genre2]* *number***\n" +
                "\t You can give any variable number of genres, if you want to introduce a genre with multiple words you will have to surround it with quotes like for example \"hip hop\"\n" +
                "\t If user is not specified if will grab your tags from your user, otherwise from the provided user\n" +
                "\t If you dont give any genres you can also specify a number and it will try to get that number of tags from your recent artists, otherwise just from your 2 songs";
    }
}
