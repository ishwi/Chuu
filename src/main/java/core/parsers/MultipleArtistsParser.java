package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.MultiExplanation;
import core.parsers.params.MultiArtistParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MultipleArtistsParser extends MultiStringParser<MultiArtistParameters> {
    private final ConcurrentLastFM lastFM;

    public MultipleArtistsParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... opts) {
        super(dao, opts);
        this.lastFM = lastFM;
    }

    @Override
    protected MultiArtistParameters doSomethingNoWords(int limit, LastFMData lastFMData, MessageReceivedEvent e) throws InstanceNotFoundException, LastFmException {
        if (isAllowUnaothorizedUsers() && lastFMData.getName() == null) {
            throw new InstanceNotFoundException(lastFMData.getDiscordId());
        }
        Set<String> artists = lastFM.getRecent(lastFMData, limit == 1 ? 1 : limit == 2 ? 10 : 1000).stream().map(NowPlayingArtist::getArtistName).distinct().limit(limit).collect(Collectors.toSet());
        return new MultiArtistParameters(e, lastFMData, artists);
    }

    @Override
    protected MultiArtistParameters doSomethingWords(LastFMData lastFMData, MessageReceivedEvent e, Set<String> strings) {
        return new MultiArtistParameters(e, lastFMData, strings);
    }

    @Override
    public List<Explanation> getUsages() {
        return MultiExplanation.obtainMultiExplanation("artist", "artists", List.of(new StrictUserExplanation()));
    }

}
