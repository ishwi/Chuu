package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.ArtistExplanation;
import core.parsers.explanation.StrictTimeframeExplanation;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ArtistTimeFrameParameters;
import core.parsers.utils.OptionalEntity;
import core.parsers.utils.Optionals;
import core.services.NPService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;

public class ArtistTimeFrameParser extends DaoParser<ArtistTimeFrameParameters> {
    private final static TimeFrameEnum defaultTFE = TimeFrameEnum.ALL;
    private final ConcurrentLastFM lastFM;
    private final boolean forComparison;

    public ArtistTimeFrameParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... otps) {
        this(dao, lastFM, false, otps);
    }

    public ArtistTimeFrameParser(ChuuService dao, ConcurrentLastFM lastFM, boolean forComparison, OptionalEntity... otps) {
        super(dao, otps);
        this.lastFM = lastFM;
        this.forComparison = forComparison;
    }

    @Override
    void setUpOptionals() {
        opts.add(Optionals.NOREDIRECT.opt);
    }

    @Override
    public ArtistTimeFrameParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User user = InteractionAux.parseUser(e);
        LastFMData datra = findLastfmFromID(user, ctx);
        TimeFrameEnum tfe = InteractionAux.parseTimeFrame(e, defaultTFE);
        return InteractionAux.processArtist(ctx, lastFM, ctx.getAuthor(), user, datra, this.wrapperFind(ctx), forComparison, isAllowUnaothorizedUsers(),
                (np, lastFMData) -> new ArtistTimeFrameParameters(ctx, np.artistName(), lastFMData, tfe),
                (s, lastFMData) -> new ArtistTimeFrameParameters(ctx, s, lastFMData, tfe));

    }

    @Override
    public ArtistTimeFrameParameters parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
        TimeFrameEnum timeFrame = defaultTFE;

        ChartParserAux chartParserAux = new ChartParserAux(words, false);
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        words = chartParserAux.getMessage();
        ParserAux parserAux = new ParserAux(words);
        User sample = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();

        LastFMData lastFMData = findLastfmFromID(sample, e);

        if (words.length == 0) {


            NowPlayingArtist np;
            if (forComparison && e.getAuthor().getIdLong() != sample.getIdLong()) {
                LastFMData lastfmFromID = findLastfmFromID(e.getAuthor(), e);
                np = new NPService(lastFM, lastfmFromID).getNowPlaying();
            } else {
                np = new NPService(lastFM, lastFMData).getNowPlaying();
            }

            return new ArtistTimeFrameParameters(e, np.artistName(), lastFMData, timeFrame);
        } else {
            return new ArtistTimeFrameParameters(e, String.join(" ", words), lastFMData, timeFrame);
        }
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new ArtistExplanation(), new StrictTimeframeExplanation(defaultTFE), new StrictUserExplanation());
    }

}
