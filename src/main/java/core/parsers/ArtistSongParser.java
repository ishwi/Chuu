package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistSongParser extends ArtistAlbumParser {

    public ArtistSongParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... o) {
        super(dao, lastFM, o);
    }

    @Override
    ArtistAlbumParameters doSomethingWithNp(NowPlayingArtist np, LastFMData lastFMData, MessageReceivedEvent e) {
        return new ArtistAlbumParameters(e, np.getArtistName(), np.getSongName(), lastFMData);
    }


}
