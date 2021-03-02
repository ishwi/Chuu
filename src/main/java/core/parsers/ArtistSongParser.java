package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistSongParser extends ArtistAlbumParser {


    public ArtistSongParser(ChuuService dao, ConcurrentLastFM lastFM, boolean forComparison, OptionalEntity... o) {
        super(dao, lastFM, forComparison, o);
    }

    public ArtistSongParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... o) {
        super(dao, lastFM, o);
    }

    @Override
    ArtistAlbumParameters doSomethingWithNp(NowPlayingArtist np, LastFMData lastFMData, MessageReceivedEvent e) {
        return new ArtistAlbumParameters(e, np.getArtistName(), np.getSongName(), lastFMData);
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.replace(5, "You need to use - to separate artist and song!");
        errorMessages
                .replace(7, "You need to add the escape character **\"\\\\\"** in the **\"-\"** that appear on the album or song.\n " +
                        "\tFor example: Artist - So**\\\\-**ng  ");

    }


}
