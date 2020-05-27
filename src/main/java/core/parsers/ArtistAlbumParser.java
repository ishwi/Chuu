package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
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
    void setUpOptionals() {
        opts.add(new OptionalEntity("--noredirect", "not change the artist name for a correction automatically"));
    }


    @Override
    public ArtistAlbumParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException, LastFmException {

        ParserAux parserAux = new ParserAux(subMessage);
        User sample = parserAux.getOneUser(e);
        subMessage = parserAux.getMessage();
        LastFMData userName = findLastfmFromID(sample, e);

        if (subMessage.length == 0) {

            NowPlayingArtist np;

            np = lastFM.getNowPlayingInfo(userName.getName());

            return doSomethingWithNp(np, userName, e);

        } else {
            return doSomethingWithString(subMessage, userName, e);
        }
    }


    ArtistAlbumParameters doSomethingWithNp(NowPlayingArtist np, LastFMData lastFMData, MessageReceivedEvent e) {
        return new ArtistAlbumParameters(e, np.getArtistName(), np.getAlbumName(), lastFMData);
    }

    ArtistAlbumParameters doSomethingWithString(String[] subMessage, LastFMData sample, MessageReceivedEvent e) {
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
        return "**" + commandName + " *artist-album* *username*** " +
                "\n\tIf username it's not provided it defaults to authors account, only ping and tag format (user#number)\n ";


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
