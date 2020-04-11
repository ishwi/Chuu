package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class ArtistAlbumParser extends DaoParser<ArtistAlbumParameters> {
    final ConcurrentLastFM lastFM;


    public ArtistAlbumParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... o) {
        super(dao);
        this.lastFM = lastFM;
        this.opts.addAll(Arrays.asList(o));
    }


    @Override
    public ArtistAlbumParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException, LastFmException {

        ParserAux parserAux = new ParserAux(subMessage);
        User sample = parserAux.getOneUser(e);
        subMessage = parserAux.getMessage();
        if (subMessage.length == 0) {

            NowPlayingArtist np;

            String userName = dao.findLastFMData(sample.getIdLong()).getName();
            np = lastFM.getNowPlayingInfo(userName);

            return doSomethingWithNp(np, sample, e);

        } else {
            return doSomethingWithString(subMessage, sample, e);
        }
    }


    ArtistAlbumParameters doSomethingWithNp(NowPlayingArtist np, User ignored, MessageReceivedEvent e) {
        return new ArtistAlbumParameters(e, np.getArtistName(), np.getAlbumName(), e.getAuthor());
    }

    ArtistAlbumParameters doSomethingWithString(String[] subMessage, User sample, MessageReceivedEvent e) {
        StringBuilder builder = new StringBuilder();
        for (String s : subMessage) {
            builder.append(s).append(" ");
        }
        String s = builder.toString();
        //To escape the "-" that could appear on some cases
        String regex = "(?<!\\\\)" + ("\\s*-\\s*");
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
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *artist-album** username* " +
               "\n\tif username its not specified it defaults to yours";

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
