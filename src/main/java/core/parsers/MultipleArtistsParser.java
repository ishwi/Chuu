package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.params.MultiArtistParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
        Set<String> collect = lastFM.getRecent(lastFMData.getName(), limit == 1 ? 1 : limit == 2 ? 10 : 1000).stream().map(NowPlayingArtist::getArtistName).distinct().limit(limit).collect(Collectors.toSet());
        return new MultiArtistParameters(e, lastFMData, collect);
    }

    @Override
    protected MultiArtistParameters doSomethingWords(LastFMData lastFMData, MessageReceivedEvent e, Set<String> strings) {
        return new MultiArtistParameters(e, lastFMData, strings);
    }

    @Override
    public String getUsageLogic(String commandName) {

        return "**" + commandName + " *[artist artist1 artist2]* *number***\n" +
                "\t You can give any variable number of artists, if you want to introduce a artist with multiple words you will have to surround it with quotes like for example \"hip hop\"\n" +
                "\t If user is not specified if will grab a artist from your user, otherwise from the provided user\n" +
                "\t If you dont give any artist you can also specify a number and it will try to get that number of artist from your recent artists, otherwise just from your last 2 artists";
    }
}
