package core.music.sources.youtube.webscrobbler.processers;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.List;

public class ChuuAudioTrackInfo extends AudioTrackInfo {
    public final List<Processed> processed;


    public ChuuAudioTrackInfo(AudioTrackInfo other, ChuuYoutubeTrackDetails cy) {
        super(cy.getProcessed().get(0).song(), cy.getProcessed().get(0).artist(), other.length, other.identifier, other.isStream, other.uri);
        this.processed = cy.getProcessed();
    }
}
