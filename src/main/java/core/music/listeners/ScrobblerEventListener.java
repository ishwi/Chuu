package core.music.listeners;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent;
import core.Chuu;
import core.music.MusicManager;
import core.music.scrobble.ExtraParamsChapter;
import core.music.scrobble.ScrobbleEventManager;
import core.music.scrobble.ScrobbleStates;
import core.music.scrobble.ScrobbleStatus;
import core.music.utils.TrackScrobble;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.time.Instant;

public class ScrobblerEventListener implements AudioEventListener {
    private final MusicManager musicManager;
    private final ScrobbleEventManager scrobbleManager;
    private int scrooblersCount = 0;

    public ScrobblerEventListener(MusicManager musicManager) {
        this.musicManager = musicManager;
        this.scrobbleManager = Chuu.getScrobbleEventManager();


    }

    @Override
    public void onEvent(AudioEvent event) {
        try {
            if (event instanceof TrackStartEvent starting) {
                hadleTrackStart(starting, false);
            }
            if (event instanceof TrackEndEvent ending) {
                handleTrackEnd(ending);
            }
        } catch (Exception e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }

    }

    private VoiceChannel getCurrentChannelEnd() {
        if (musicManager.getChannelId() != null) {
            return musicManager.getGuild().getVoiceChannelById(musicManager.getChannelId());
        } else return getCurrentChannel();
    }

    private VoiceChannel getCurrentChannel() {
        GuildVoiceState voiceState = musicManager.getGuild().getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel() || voiceState.getChannel() == null)
            return null;
        return voiceState.getChannel();
    }

    public void signalChapterEnd(TrackScrobble current, long ms, long fms, long baseLine) {
        this.scrobbleManager
                .submitEvent(new ScrobbleStatus(ScrobbleStates.CHAPTER_CHANGE, current, this::getCurrentChannel, musicManager.getGuildId(), Instant.now(), (a, b) -> {
                }, new ExtraParamsChapter(ms, fms, baseLine)));
    }

    public void signalMetadataChange(TrackScrobble newMetadata) {
        this.scrobbleManager.submitEvent(new ScrobbleStatus(ScrobbleStates.METADATA_CHANGE, newMetadata, this::getCurrentChannel, musicManager.getGuildId(), Instant.now(), (a, b) -> {
        }));
    }

    public void hadleTrackStart(TrackStartEvent event, boolean inmediato) {
        Instant start = Instant.now();
        this.musicManager.getTrackScrobble(event.track).thenAccept(z ->
                this.scrobbleManager.submitEvent(new ScrobbleStatus(ScrobbleStates.SCROBBLING, z, this::getCurrentChannel, musicManager.getGuildId(), start, (status, lastFMData) ->
                        this.scrooblersCount = lastFMData.size()
                )));

    }

    private void handleTrackEnd(TrackEndEvent event) {
        Instant end = Instant.now();
        this.musicManager.getTrackScrobble(event.track).thenAccept(z ->
                this.scrobbleManager.submitEvent(new ScrobbleStatus(ScrobbleStates.FINISHED, z, this::getCurrentChannelEnd, musicManager.getGuildId(), end, (status, lastFMData) ->
                        scrooblersCount = lastFMData.size()
                )));

    }


    public int getScrooblersCount() {
        return scrooblersCount;
    }
}
