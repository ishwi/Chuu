package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.AlbumTimeFrameParameters;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.params.ArtistTimeFrameParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AlbumTimeFrameParser extends DaoParser<AlbumTimeFrameParameters> {

    private final static TimeFrameEnum defaultTFE = TimeFrameEnum.ALL;
    private final ConcurrentLastFM lastFM;
    private final ArtistAlbumParser innerParser;

    public AlbumTimeFrameParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... otps) {
        super(dao, otps);
        innerParser = new ArtistAlbumParser(dao, lastFM);
        this.lastFM = lastFM;
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("noredirect", "not change the artist name for a correction automatically"));
    }

    @Override
    public AlbumTimeFrameParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        TimeFrameEnum timeFrame = defaultTFE;

        ChartParserAux chartParserAux = new ChartParserAux(words, false);
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        words = chartParserAux.getMessage();
        ParserAux parserAux = new ParserAux(words);
        User sample = parserAux.getOneUser(e);
        words = parserAux.getMessage();

        LastFMData lastFMData = findLastfmFromID(sample, e);

        if (words.length == 0) {
            NowPlayingArtist np = lastFM.getNowPlayingInfo(lastFMData.getName());
            return new AlbumTimeFrameParameters(e, np.getArtistName(), np.getAlbumName(), lastFMData, timeFrame);
        } else {
            ArtistAlbumParameters artistAlbumParameters = innerParser.doSomethingWithString(words, lastFMData, e);
            return new AlbumTimeFrameParameters(e, artistAlbumParameters.getArtist(), artistAlbumParameters.getAlbum(), lastFMData, timeFrame);
        }
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "You need to use - to separate artist and album!");
        errorMessages
                .put(7, "You need to add the escape character **\"\\\\\"** in the **\"-\"** that appear on the album or artist.\n " +
                        "\tFor example: Artist - Alb**\\\\-**um  ");

    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *artist - album* *[d,w,m,q,s,y]* *username***\n" +
                "\tIf a timeframe it's not specified defaults to " + defaultTFE.toString() + "\n" +
                "\tIf an username it's not provided it defaults to authors account, only ping and tag format (user#number)\n " +
                "\tDue to being able to provide an artist name and the timeframe, some" +
                " conflicts may occur if the timeframe keyword appears on the artist name, to reduce possible" +
                " conflicts only the one letter shorthand is available for the timeframe, the [a] shorthand is also disabled to reduce more conflicts " +
                "since its the default time frame applied\n";
    }
}
