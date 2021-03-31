package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.explanation.AlbumExplanation;
import core.parsers.explanation.StrictTimeframeExplanation;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.AlbumTimeFrameParameters;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.CustomTimeFrame;
import core.services.NPService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

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
        User sample = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();

        LastFMData lastFMData = findLastfmFromID(sample, e);

        if (words.length == 0) {
            if (lastFMData.getName() == null) {
                throw new InstanceNotFoundException(sample.getIdLong());
            }
            NowPlayingArtist np = new NPService(lastFM, lastFMData).getNowPlaying();
            return new AlbumTimeFrameParameters(e, np.artistName(), np.albumName(), lastFMData, new CustomTimeFrame(timeFrame));
        } else {
            ArtistAlbumParameters artistAlbumParameters = innerParser.doSomethingWithString(words, lastFMData, e);
            if (artistAlbumParameters == null) {
                return null;
            }
            return new AlbumTimeFrameParameters(e, artistAlbumParameters.getArtist(), artistAlbumParameters.getAlbum(), lastFMData, new CustomTimeFrame(timeFrame));
        }
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new AlbumExplanation(), new StrictTimeframeExplanation(defaultTFE), new StrictUserExplanation());
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "You need to use - to separate artist and album!");
        errorMessages
                .put(7, "You need to add the escape character **\"\\\\\"** in the **\"-\"** that appear on the album or artist.\n " +
                        "\tFor example: Artist - Alb**\\\\-**um  ");

    }

}
