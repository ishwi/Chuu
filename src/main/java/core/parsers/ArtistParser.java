package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.ArtistExplanation;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ArtistParameters;
import core.services.NPService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Arrays;
import java.util.List;

public class ArtistParser extends DaoParser<ArtistParameters> {
    final ConcurrentLastFM lastFM;
    private final boolean forComparison;


    public ArtistParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... strings) {
        this(dao, lastFM, true, strings);
    }

    public ArtistParser(ChuuService dao, ConcurrentLastFM lastFM, boolean forComparison, OptionalEntity... strings) {
        super(dao);
        this.lastFM = lastFM;
        this.forComparison = forComparison;
        opts.addAll(Arrays.asList(strings));
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("noredirect", "not use autocorrections"));
    }


    @Override
    public ArtistParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        var artist = e.getOption(ArtistExplanation.NAME);
        User oneUser = InteractionAux.parseUser(e);

        LastFMData data = findLastfmFromID(oneUser, ctx);
        if (artist == null) {
            NowPlayingArtist np;
            if (isAllowUnaothorizedUsers() && data.getName() == null) {
                throw new InstanceNotFoundException(oneUser.getIdLong());
            }
            try {
                if (forComparison && ctx.getAuthor().getIdLong() != oneUser.getIdLong()) {
                    LastFMData lastfmFromID = findLastfmFromID(ctx.getAuthor(), ctx);
                    np = new NPService(lastFM, lastfmFromID).getNowPlaying();
                } else {
                    np = new NPService(lastFM, data).getNowPlaying();
                }
            } catch (InstanceNotFoundException ex) {
                np = new NPService(lastFM, data).getNowPlaying();
            }
            return new ArtistParameters(ctx, np.artistName(), data);
        } else {
            return new ArtistParameters(ctx, String.join(" ", artist.getAsString()), data);
        }
    }

    @Override
    protected ArtistParameters parseLogic(Context e, String[] words) throws
            InstanceNotFoundException, LastFmException {
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();

        LastFMData data = findLastfmFromID(oneUser, e);
        if (words.length == 0) {
            NowPlayingArtist np;
            if (isAllowUnaothorizedUsers() && data.getName() == null) {
                throw new InstanceNotFoundException(oneUser.getIdLong());
            }
            try {
                if (forComparison && e.getAuthor().getIdLong() != oneUser.getIdLong()) {
                    LastFMData lastfmFromID = findLastfmFromID(e.getAuthor(), e);
                    np = new NPService(lastFM, lastfmFromID).getNowPlaying();
                } else {
                    np = new NPService(lastFM, data).getNowPlaying();
                }
            } catch (InstanceNotFoundException ex) {
                np = new NPService(lastFM, data).getNowPlaying();
            }
            return new ArtistParameters(e, np.artistName(), data);
        } else {
            return new ArtistParameters(e, String.join(" ", words), data);
        }
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new ArtistExplanation(), new StrictUserExplanation());
    }


}
