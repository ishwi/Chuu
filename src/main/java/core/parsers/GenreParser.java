package core.parsers;

import core.apis.ExecutorsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.explanation.GenreExplanation;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.GenreParameters;
import core.services.NPService;
import core.services.tracklist.TagStorer;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class GenreParser extends DaoParser<GenreParameters> {
    private final ConcurrentLastFM lastFM;
    private final ExecutorService executor;

    public GenreParser(ChuuService service, ConcurrentLastFM lastFM) {
        super(service);
        this.lastFM = lastFM;
        executor = ExecutorsSingleton.getInstance();
    }

    @Override
    protected GenreParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        String genre;
        NowPlayingArtist nowPlayingInfo = null;
        LastFMData lastFMData;
        ParserAux parserAux = new ParserAux(words);
        User user = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();

        boolean autoDetected = false;
        if (words.length == 0) {
            lastFMData = findLastfmFromID(user, e);
            nowPlayingInfo = new NPService(lastFM, lastFMData).getNowPlaying();
            List<String> tags = new TagStorer(dao, lastFM, executor, nowPlayingInfo).findTags();
            if (tags.isEmpty()) {
                sendError("Was not able to find any tags on your now playing song/album/artist: "
                          + String.format("%s - %s | %s", nowPlayingInfo.getArtistName(), nowPlayingInfo.getSongName(), nowPlayingInfo.getAlbumName())
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
