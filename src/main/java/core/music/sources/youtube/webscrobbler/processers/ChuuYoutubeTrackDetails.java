package core.music.sources.youtube.webscrobbler.processers;

import com.sedmelluq.discord.lavaplayer.source.youtube.DefaultYoutubeTrackDetails;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackJsonData;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.List;

public class ChuuYoutubeTrackDetails extends DefaultYoutubeTrackDetails {
    private final List<Processed> processed;
    private ChuuAudioTrackInfo intercepted = null;

    public ChuuYoutubeTrackDetails(String videoId, YoutubeTrackJsonData data, List<Processed> processed) {
        super(videoId, data);
        this.processed = processed;
    }

    public List<Processed> getProcessed() {
        return processed;
    }

    @Override
    public AudioTrackInfo getTrackInfo() {
        if (intercepted == null) {
            intercepted = new ChuuAudioTrackInfo(super.getTrackInfo(), this);
        }
        return intercepted;
    }

}
