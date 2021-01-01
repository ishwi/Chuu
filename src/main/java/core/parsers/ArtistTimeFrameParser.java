package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistTimeFrameParameters;
import core.services.NPService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistTimeFrameParser extends DaoParser<ArtistTimeFrameParameters> {
    private final static TimeFrameEnum defaultTFE = TimeFrameEnum.ALL;
    private final ConcurrentLastFM lastFM;

    public ArtistTimeFrameParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... otps) {
        super(dao, otps);
        this.lastFM = lastFM;
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("noredirect", "not change the artist name for a correction automatically"));
    }

    @Override
    public ArtistTimeFrameParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        TimeFrameEnum timeFrame = defaultTFE;

        ChartParserAux chartParserAux = new ChartParserAux(words, false);
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        words = chartParserAux.getMessage();
        ParserAux parserAux = new ParserAux(words);
        User sample = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();

        LastFMData lastFMData = findLastfmFromID(sample, e);

        if (words.length == 0) {
            NowPlayingArtist np = new NPService(lastFM, lastFMData).getNowPlaying();
            return new ArtistTimeFrameParameters(e, np.getArtistName(), lastFMData, timeFrame);
        } else {
            return new ArtistTimeFrameParameters(e, String.join(" ", words), lastFMData, timeFrame);
        }
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *artist* *[d,w,m,q,s,y]* *username***\n" +
                "\tIf a timeframe it's not specified defaults to " + defaultTFE.toString() + "\n" +
                "\tIf an username it's not provided it defaults to authors account, only ping, tag format (user#number),discord id, u:username or lfm:lastfmname\n " +
                "\tDue to being able to provide an artist name and the timeframe, some" +
                " conflicts may occur if the timeframe keyword appears on the artist name, to reduce possible" +
                " conflicts only the one letter shorthand is available for the timeframe, the [a] shorthand is also disabled to reduce more conflicts " +
                "since its the default time frame applied\n";
    }
}
