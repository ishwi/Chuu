package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistTimeFrameParameters;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
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
    public ArtistTimeFrameParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        TimeFrameEnum timeFrame = defaultTFE;

        ChartParserAux chartParserAux = new ChartParserAux(words, false);
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        words = chartParserAux.getMessage();
        ParserAux parserAux = new ParserAux(words);
        User sample = parserAux.getOneUser(e);
        words = parserAux.getMessage();

        if (words.length == 0) {
            String userName = dao.findLastFMData(sample.getIdLong()).getName();
            NowPlayingArtist np = lastFM.getNowPlayingInfo(userName);
            return new ArtistTimeFrameParameters(e, np.getArtistName(), sample, timeFrame);
        } else {
            return new ArtistTimeFrameParameters(e, String.join(" ", words), sample, timeFrame);
        }
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *artist* *[d,w,m,q,s,y]*** \n" +
               "\tIf time is not specified defaults to " + defaultTFE.toString() + "\n" +
               "\tDue to being able to provide an artist name and the timeframe, some" +
               " conflicts may occur if the timeframe keyword appears on the artist name, to reduce possible" +
               " conflicts only the one letter shorthand is available for the timeframe, the [a] shorthand is also disabled to reduce more conflicts " +
                "since its the default time frame applied";
    }
}
