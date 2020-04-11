package core.parsers.params;

import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NowPlayingParameters extends ChuuDataParams {
    private final NowPlayingArtist nowPlayingArtist;

    public NowPlayingParameters(MessageReceivedEvent e, LastFMData lastFMData, NowPlayingArtist nowPlayingArtist) {
        super(e, lastFMData);
        this.nowPlayingArtist = nowPlayingArtist;
    }

    public NowPlayingArtist getNowPlayingArtist() {
        return nowPlayingArtist;
    }
}
