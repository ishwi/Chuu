package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.exceptions.LastFmException;
import core.parsers.params.MultiArtistParameters;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;

import java.util.Set;
import java.util.stream.Collectors;

public class MultipleArtistsParser extends MultiStringParser<MultiArtistParameters> {
    private final ConcurrentLastFM lastFM;

    public MultipleArtistsParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... opts) {
        super(dao, "artist", "artists", opts);
        this.lastFM = lastFM;
    }

    @Override
    protected MultiArtistParameters doSomethingNoWords(int limit, LastFMData lastFMData, Context e) throws InstanceNotFoundException, LastFmException {
        if (isAllowUnaothorizedUsers() && lastFMData.getName() == null) {
            throw new InstanceNotFoundException(lastFMData.getDiscordId());
        }
        Set<String> artists = lastFM.getRecent(lastFMData, limit == 1 ? 1 : limit == 2 ? 10 : 1000).stream().map(NowPlayingArtist::artistName).distinct().limit(limit).collect(Collectors.toSet());
        return new MultiArtistParameters(e, lastFMData, artists);
    }

    @Override
    protected MultiArtistParameters doSomethingWords(LastFMData lastFMData, Context e, Set<String> strings) {
        return new MultiArtistParameters(e, lastFMData, strings);
    }

}
