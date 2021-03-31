package core.parsers;

import core.apis.ExecutorsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.MultiExplanation;
import core.parsers.params.MultipleGenresParameters;
import core.services.NPService;
import core.services.tracklist.TagStorer;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
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
    public List<Explanation> getUsages() {
        return MultiExplanation.obtainMultiExplanation("genre", "genres", List.of(new StrictUserExplanation()));

    }

    @Override
    protected MultipleGenresParameters doSomethingNoWords(int limit, LastFMData lastFMData, MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        NowPlayingArtist nowPlayingInfo = new NPService(lastFM, lastFMData).getNowPlaying();
        List<String> tags = new TagStorer(dao, lastFM, executor, nowPlayingInfo).findTags();
        if (tags.isEmpty()) {
            sendError("Was not able to find any tags on your now playing song/album/artist: "
                      + String.format("%s - %s | %s", nowPlayingInfo.getArtistName(), nowPlayingInfo.getSongName(), nowPlayingInfo.getAlbumName())
                    , e);
            return null;
        }
        return new MultipleGenresParameters(e, lastFMData, new HashSet<>(tags));
    }

    @Override
    protected MultipleGenresParameters doSomethingWords(LastFMData lastFMData, MessageReceivedEvent e, Set<String> strings) {
        return new MultipleGenresParameters(e, lastFMData, strings);

    }

}
