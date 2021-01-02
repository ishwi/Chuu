package core.music.utils;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.music.MusicManager;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScrobblerEventListener implements AudioEventListener {
    private final MusicManager musicManager;
    private final ChuuService db;
    private final ConcurrentLastFM lastFM;
    private Instant instant;

    public ScrobblerEventListener(MusicManager musicManager, ConcurrentLastFM lastFM) {
        this.musicManager = musicManager;
        this.lastFM = lastFM;
        db = Chuu.getDao();
    }

    @Override
    public void onEvent(AudioEvent event) {
        try {
            if (event instanceof TrackStartEvent starting) {
                hadleTrackStart(starting);
            }
            if (event instanceof TrackEndEvent ending) {
                handleTrackEnd(ending);
            }
        } catch (Exception e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }

    }

    private void hadleTrackStart(TrackStartEvent event) throws LastFmException {
        this.instant = OffsetDateTime.now(ZoneOffset.UTC).toInstant();
        AudioTrack playingTrack = event.player.getPlayingTrack();
        GuildVoiceState voiceState = musicManager.getGuild().getSelfMember().getVoiceState();
        assert voiceState != null && voiceState.inVoiceChannel() && voiceState.getChannel() != null;
        VoiceChannel channel = voiceState.getChannel();
        List<Long> collect = channel.getMembers().stream().mapToLong(ISnowflake::getIdLong).boxed().collect(Collectors.toList());
        Set<LastFMData> scrobbleableUsers = db.findScrobbleableUsers(channel.getIdLong()).stream().filter(x -> collect.contains(x.getDiscordId())).collect(Collectors.toSet());
        AudioTrackInfo info = playingTrack.getInfo();
        String title = info.title;
        String author = info.author;

        Scrobble scrobble = new Scrobble(info.author, null, info.title);

        for (LastFMData data : scrobbleableUsers) {
            lastFM.flagNP(data.getSession(), scrobble);
        }

    }

    private void handleTrackEnd(TrackEndEvent event) throws LastFmException {
        try {
            AudioTrack playingTrack = event.player.getPlayingTrack();
            GuildVoiceState voiceState = musicManager.getGuild().getSelfMember().getVoiceState();
            assert voiceState != null && voiceState.inVoiceChannel() && voiceState.getChannel() != null;
            VoiceChannel channel = voiceState.getChannel();
            List<Long> collect = channel.getMembers().stream().mapToLong(ISnowflake::getIdLong).boxed().collect(Collectors.toList());
            Set<LastFMData> scrobbleableUsers = db.findScrobbleableUsers(channel.getIdLong()).stream().filter(x -> collect.contains(x.getDiscordId())).collect(Collectors.toSet());
            AudioTrackInfo info = playingTrack.getInfo();
            String title = info.title;
            String author = info.author;

            Scrobble scrobble = new Scrobble(info.author, null, info.title);
            if (instant == null) {
                this.instant = OffsetDateTime.now(ZoneOffset.UTC).toInstant();
            }
            for (LastFMData data : scrobbleableUsers) {
                lastFM.scrobble(data.getSession(), scrobble, instant);
            }
        } finally {
            instant = null;
        }
    }


}
