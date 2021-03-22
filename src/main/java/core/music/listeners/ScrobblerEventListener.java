package core.music.listeners;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.entities.Scrobble;
import core.exceptions.LastFmException;
import core.music.MusicManager;
import core.music.sources.MetadataTrack;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.Metadata;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ScrobblerEventListener implements AudioEventListener {
    private final MusicManager musicManager;
    private final ChuuService db;
    private final ConcurrentLastFM lastFM;
    private final static ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();
    private final List<ScheduledFuture<?>> todo = new ArrayList<>();
    private final Map<AudioTrack, Instant> startMap = new HashMap<>();
    private int scrooblersCount = 0;

    public ScrobblerEventListener(MusicManager musicManager, ConcurrentLastFM lastFM) {
        this.musicManager = musicManager;
        this.lastFM = lastFM;

        db = Chuu.getDao();
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

    public void hadleTrackStart(TrackStartEvent event, boolean inmediato) {
        if (event.track.getDuration() == Long.MAX_VALUE) {
            return;
        }
        this.startMap.put(event.track, OffsetDateTime.now(ZoneOffset.UTC).toInstant());

        Runnable runnable = () -> {
            try {
                AudioTrack playingTrack = event.player.getPlayingTrack();
                GuildVoiceState voiceState = musicManager.getGuild().getSelfMember().getVoiceState();
                if (voiceState == null || !voiceState.inVoiceChannel() || voiceState.getChannel() == null)
                    return;
                VoiceChannel channel = voiceState.getChannel();
                List<Long> voiceMembers = channel.getMembers().stream().mapToLong(ISnowflake::getIdLong).boxed().toList();
                Set<LastFMData> scrobbleableUsers = db.findScrobbleableUsers(channel.getGuild().getIdLong()).stream().filter(x -> voiceMembers.contains(x.getDiscordId())).collect(Collectors.toSet());


                Scrobble scrobble = obtainScrobble(playingTrack, false);

                if (Chuu.chuuSess != null) {
                    lastFM.flagNP(Chuu.chuuSess, scrobble);
                }
                for (LastFMData data : scrobbleableUsers) {
                    lastFM.flagNP(data.getSession(), scrobble);

                }
                scrooblersCount = scrobbleableUsers.size();
            } catch (LastFmException exception) {
                Chuu.getLogger().warn(exception.getMessage(), exception);
                this.todo.forEach(x -> x.cancel(false));
                this.todo.clear();
            }
        };
        CompletableFuture.delayedExecutor(inmediato ? 1 : 5, TimeUnit.SECONDS).execute(() -> {
            ScheduledFuture<?> scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(runnable, 0, 60, TimeUnit.SECONDS);
            todo.add(scheduledFuture);
            CompletableFuture.delayedExecutor(event.track.getDuration() - 5, TimeUnit.MILLISECONDS).execute(() -> scheduledFuture.cancel(true));
        });

    }

    private void handleTrackEnd(TrackEndEvent event) throws LastFmException {
        Instant start = Optional.ofNullable(this.startMap.remove(event.track)).orElse(Instant.now());
        long seconds = Instant.now().getEpochSecond() - start.getEpochSecond();
        if (seconds < 30 || (seconds < (event.track.getDuration() / 1000) / 2 && seconds < 4 * 60)) {
            Chuu.getLogger().info("Didnt scrobble {}: Duration {}", event.track.getIdentifier(), seconds);
            return;
        }
        try {
            AudioTrack playingTrack = event.track;
            GuildVoiceState voiceState = musicManager.getGuild().getSelfMember().getVoiceState();
            assert voiceState != null && voiceState.inVoiceChannel() && voiceState.getChannel() != null;
            VoiceChannel channel = voiceState.getChannel();
            List<Long> collect = channel.getMembers().stream().mapToLong(ISnowflake::getIdLong).boxed().toList();
            Set<LastFMData> scrobbleableUsers = db.findScrobbleableUsers(channel.getGuild().getIdLong()).stream().filter(x -> collect.contains(x.getDiscordId())).collect(Collectors.toSet());

            Scrobble scrobble = obtainScrobble(playingTrack, true);

            for (LastFMData data : scrobbleableUsers) {
                lastFM.scrobble(data.getSession(), scrobble, start);
            }
            if (Chuu.chuuSess != null) {
                lastFM.scrobble(Chuu.chuuSess, scrobble, start);
            }

        } finally {
            scrooblersCount = 0;
            this.todo.forEach(x -> x.cancel(true));
            this.todo.clear();
        }
    }

    private Scrobble obtainScrobble(AudioTrack playingTrack, boolean hasEnded) {
        Metadata metadata = hasEnded ? musicManager.getLastMetada() : musicManager.getMetadata();
        String album = null;
        String image = null;
        if (playingTrack instanceof MetadataTrack spo) {
            album = spo.getAlbum();
            image = spo.getImage();
        }
        AudioTrackInfo info = playingTrack.getInfo();
        String title = metadata != null ? metadata.song() : info.title;
        String author = metadata != null ? metadata.artist() : info.author;
        album = metadata != null ? metadata.album() : album;
        image = metadata != null ? metadata.image() : image;
        return new Scrobble(author, album, title, image);

    }

    public int getScrooblersCount() {
        return scrooblersCount;
    }
}
