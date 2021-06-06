package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.AlbumExplanation;
import core.parsers.explanation.StrictTimeframeExplanation;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

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
        opts.add(new OptionalEntity("noredirect", "not use autocorrections"));
    }

    @Override
    public AlbumTimeFrameParameters parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
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
        AlbumExplanation alb = new AlbumExplanation();
        return List.of(alb.artist(), alb.album(), new StrictTimeframeExplanation(defaultTFE), new StrictUserExplanation());
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
    public AlbumTimeFrameParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        InteractionAux.ArtistAlbum artistAlbum = InteractionAux.parseAlbum(e, () -> sendError(this.getErrorMessage(8), ctx));
        if (artistAlbum == null) {
            return null;
        }
        TimeFrameEnum timeFrameEnum = InteractionAux.parseTimeFrame(e, TimeFrameEnum.ALL);

        User oneUser = InteractionAux.parseUser(e);
        LastFMData userName = findLastfmFromID(oneUser, ctx);
        var ap = InteractionAux.processAlbum(artistAlbum,
                lastFM,
                userName,
                true,
                ctx.getAuthor(),
                oneUser,
                this.wrapperFind(ctx),
                (nowPlayingArtist, lastFMData) -> innerParser.doSomethingWithNp(nowPlayingArtist, lastFMData, ctx),
                (s, lastFMData) -> innerParser.doSomethingWithString(s, lastFMData, ctx));
        if (ap == null) {
            return null;
        }
        return new AlbumTimeFrameParameters(ctx, ap.getArtist(), ap.getAlbum(), userName, new CustomTimeFrame(timeFrameEnum));
    }


}
