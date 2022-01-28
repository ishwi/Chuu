package core.parsers;

import core.apis.ExecutorsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.GenreExplanation;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.GenreParameters;
import core.parsers.utils.OptionalEntity;
import core.services.NPService;
import core.services.tags.TagStorer;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.apache.commons.text.WordUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class GenreParser extends DaoParser<GenreParameters> {
    private final ConcurrentLastFM lastFM;
    private final ExecutorService executor;

    public GenreParser(ChuuService service, ConcurrentLastFM lastFM, OptionalEntity... opts) {
        super(service, opts);
        this.lastFM = lastFM;
        executor = ExecutorsSingleton.getInstance();
    }

    @Override
    public GenreParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        CommandInteraction e = ctx.e();
        User user = InteractionAux.parseUser(e);
        LastFMData data = findLastfmFromID(user, ctx);
        NowPlayingArtist nowPlayingInfo = null;
        String genre = Optional.ofNullable(e.getOption("genre")).map(OptionMapping::getAsString).orElse(null);

        boolean autoDetected = genre == null;
        if (autoDetected) {
            nowPlayingInfo = new NPService(lastFM, data).getNowPlaying();
            List<String> tags = new TagStorer(dao, lastFM, executor, nowPlayingInfo).findTags();
            if (tags.isEmpty()) {
                sendError("Was not able to find any tags on your now playing song/album/artist: "
                                + String.format("%s - %s | %s", nowPlayingInfo.artistName(), nowPlayingInfo.songName(), nowPlayingInfo.albumName())
                        , ctx);
                return null;
            }
            genre = tags.get(0);
        }
        return new GenreParameters(ctx, WordUtils.capitalizeFully(genre), autoDetected, nowPlayingInfo, data, user);
    }

    @Override
    protected GenreParameters parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
        String genre;
        LastFMData lastFMData;
        ParserAux parserAux = new ParserAux(words);
        User user = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();
        NowPlayingArtist nowPlayingInfo = null;
        boolean autoDetected = false;
        if (words.length == 0) {
            lastFMData = findLastfmFromID(user, e);
            nowPlayingInfo = new NPService(lastFM, lastFMData).getNowPlaying();
            List<String> tags = new TagStorer(dao, lastFM, executor, nowPlayingInfo).findTags();
            if (tags.isEmpty()) {
                sendError("Was not able to find any tags on your now playing song/album/artist: "
                                + String.format("%s - %s | %s", nowPlayingInfo.artistName(), nowPlayingInfo.songName(), nowPlayingInfo.albumName())
                        , e);
                return null;
            }
            autoDetected = true;
            genre = tags.get(0);
        } else {
            User oneUser = parserAux.getOneUser(e, dao);
            words = parserAux.getMessage();
            genre = String.join(" ", words);
            lastFMData = findLastfmFromID(user, e);
        }
        return new GenreParameters(e, WordUtils.capitalizeFully(genre), autoDetected, nowPlayingInfo, lastFMData, user);

    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new GenreExplanation(), new StrictUserExplanation());
    }


}
