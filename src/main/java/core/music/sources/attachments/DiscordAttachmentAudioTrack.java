package core.music.sources.attachments;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.net.URI;

public final class DiscordAttachmentAudioTrack extends DelegatedAudioTrack {
    private MediaContainerDescriptor containerTrackFactory;
    private DiscordAttachmentAudioSourceManager sourceManager;


    public DiscordAttachmentAudioTrack(AudioTrackInfo discordTrackInfo) {
        super(discordTrackInfo);
    }

    public DiscordAttachmentAudioTrack(AudioTrackInfo discordTrackInfo, MediaContainerDescriptor containerTrackFactory, DiscordAttachmentAudioSourceManager sourceManager) {
        super(discordTrackInfo);
        this.containerTrackFactory = containerTrackFactory;
        this.sourceManager = sourceManager;
    }

    public AudioTrackInfo discordTrackInfo() {
        return trackInfo;
    }

    public MediaContainerDescriptor containerDescriptor() {
        return containerTrackFactory;
    }

    public DiscordAttachmentAudioSourceManager sourceManager() {
        return sourceManager;
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new DiscordAttachmentAudioTrack(trackInfo, containerTrackFactory, sourceManager);
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        try (var httpInterface = sourceManager.getHttpInterface()) {
            try (var stream = new PersistentHttpStream(httpInterface, new URI(trackInfo.identifier), Units.CONTENT_LENGTH_UNKNOWN)) {
                processDelegate((InternalAudioTrack) containerTrackFactory.createTrack(trackInfo, stream), executor);
            }
        }
    }
}
