package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.AlbumExplanation;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ArtistAlbumParameters;
import core.services.NPService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ArtistAlbumParser extends DaoParser<ArtistAlbumParameters> {

    final ConcurrentLastFM lastFM;
    private final boolean forComparison;
    String slashName = AlbumExplanation.NAME;

    public ArtistAlbumParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... o) {
        this(dao, lastFM, true, o);
    }

    public ArtistAlbumParser(ChuuService dao, ConcurrentLastFM lastFM, boolean forComparison, OptionalEntity... o) {
        super(dao);
        this.lastFM = lastFM;
        this.forComparison = forComparison;
        this.opts.addAll(Arrays.asList(o));
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("noredirect", "not use autocorrections"));
    }


    @Override
    public ArtistAlbumParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User oneUser = InteractionAux.parseUser(e);
        LastFMData userName = findLastfmFromID(oneUser, ctx);
        InteractionAux.ArtistAlbum artistAlbum = InteractionAux.parseCommonArtistAlbum(() -> sendError(this.getErrorMessage(8), ctx), e, slashName);
        if (artistAlbum == null) {
            return null;
        }
        return InteractionAux.processAlbum(artistAlbum,
                lastFM,
                userName,
                true,
                ctx.getAuthor(),
                oneUser,
                this.wrapperFind(ctx),
                (nowPlayingArtist, lastFMData) -> doSomethingWithNp(nowPlayingArtist, lastFMData, ctx),
                (s, lastFMData) -> doSomethingWithString(s, lastFMData, ctx));


    }

    @Override
    public ArtistAlbumParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException, LastFmException {

        ParserAux parserAux = new ParserAux(subMessage);
        User sample = parserAux.getOneUser(e, dao);
        subMessage = parserAux.getMessage();
        LastFMData userName = findLastfmFromID(sample, e);

        if (subMessage.length == 0) {
            NowPlayingArtist np;
            try {
                if (forComparison && e.getAuthor().getIdLong() != sample.getIdLong()) {
                    LastFMData lastfmFromID = findLastfmFromID(e.getAuthor(), e);
                    np = new NPService(lastFM, lastfmFromID).getNowPlaying();
                } else {
                    np = new NPService(lastFM, userName).getNowPlaying();
                }
            } catch (InstanceNotFoundException ex) {
                np = new NPService(lastFM, userName).getNowPlaying();
            }

            return doSomethingWithNp(np, userName, e);

        } else {
            return doSomethingWithString(subMessage, userName, e);
        }
    }

    @Override
    public List<Explanation> getUsages() {
        AlbumExplanation alb = new AlbumExplanation();
        return List.of(alb.artist(), alb.album(), new StrictUserExplanation());
    }


    ArtistAlbumParameters doSomethingWithNp(NowPlayingArtist np, LastFMData lastFMData, Context e) {
        return new ArtistAlbumParameters(e, np.artistName(), np.albumName(), lastFMData);
    }

    ArtistAlbumParameters doSomethingWithString(String[] subMessage, LastFMData sample, Context e) {
        StringBuilder builder = new StringBuilder();
        for (String s : subMessage) {
            builder.append(s).append(" ");
        }
        String s = builder.toString();
        //To escape the "-" that could appear on some cases
        String regex = "(?<!\\\\)\\s*-\\s*";
        String[] content = s.split(regex);
        content = Arrays.stream(content).filter(Predicate.not(StringUtils::isBlank)).toArray(String[]::new);
        if (content.length < 2) {
            sendError(this.getErrorMessage(5), e);
            return null;
        }
        if (content.length > 2) {
            sendError(this.getErrorMessage(7), e);
            return null;
        }

        String artist = content[0].trim().replaceAll("\\\\-", "-");
        String album = content[1].trim().replaceAll("\\\\-", "-");
        return new ArtistAlbumParameters(e, artist, album, sample);
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "You need to use - to separate artist and album!");
        errorMessages.put(8, "Need both the artist and the album!");
        errorMessages
                .put(7, "You need to add the escape character **\"\\\\\"** in the **\"-\"** that appear on the album or artist.\n " +
                        "\tFor example: Artist - Alb**\\\\-**um  ");

    }
}
