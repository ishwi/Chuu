package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.explanation.AlbumExplanation;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.ArtistAlbumParameters;
import core.services.NPService;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class ArtistAlbumParser extends DaoParser<ArtistAlbumParameters> {
    final ConcurrentLastFM lastFM;
    private final boolean forComparison;

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
        opts.add(new OptionalEntity("noredirect", "not change the artist name for a correction automatically"));
    }


    @Override
    public ArtistAlbumParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException, LastFmException {

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
        return List.of(new AlbumExplanation(), new StrictUserExplanation());
    }


    ArtistAlbumParameters doSomethingWithNp(NowPlayingArtist np, LastFMData lastFMData, MessageReceivedEvent e) {
        return new ArtistAlbumParameters(e, np.artistName(), np.albumName(), lastFMData);
    }

    ArtistAlbumParameters doSomethingWithString(String[] subMessage, LastFMData sample, MessageReceivedEvent e) {
        StringBuilder builder = new StringBuilder();
        for (String s : subMessage) {
            builder.append(s).append(" ");
        }
        String s = builder.toString();
        //To escape the "-" that could appear on some cases
        String regex = "(?<!\\\\)\\s*-\\s*";
        String[] content = s.split(regex);

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
        errorMessages
                .put(7, "You need to add the escape character **\"\\\\\"** in the **\"-\"** that appear on the album or artist.\n " +
                        "\tFor example: Artist - Alb**\\\\-**um  ");

    }
}
