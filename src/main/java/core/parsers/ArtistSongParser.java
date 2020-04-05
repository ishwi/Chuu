package core.parsers;

import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistSongParser extends ArtistAlbumParser {
    public ArtistSongParser(ChuuService dao, ConcurrentLastFM lastFM) {
        super(dao, lastFM);
    }

    public ArtistSongParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... o) {
        super(dao, lastFM, o);
    }

    @Override
    String[] doSomethingWithNp(NowPlayingArtist np, User ignored, MessageReceivedEvent e) {
        return new String[]{np.getArtistName(), np.getSongName(), String.valueOf(e.getAuthor().getIdLong())};
    }


}
