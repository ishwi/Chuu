package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistSongParser extends ArtistAlbumParser {

    public ArtistSongParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... o) {
        super(dao, lastFM, o);
    }

    @Override
    ArtistAlbumParameters doSomethingWithNp(NowPlayingArtist np, User ignored, MessageReceivedEvent e) {
        return new ArtistAlbumParameters(e, np.getArtistName(), np.getSongName(), e.getAuthor());
    }


}
