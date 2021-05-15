package core.music.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sedmelluq.discord.lavaplayer.source.youtube.ChuuYoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import core.Chuu;
import core.music.sources.MetadataTrack;
import core.music.sources.youtube.webscrobbler.processers.ChuuAudioTrackInfo;
import core.music.sources.youtube.webscrobbler.processers.Processed;
import core.services.AlbumFinder;
import dao.entities.Album;
import dao.entities.Metadata;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record ScrobbleProcesser(AlbumFinder albumFinder) {
    public static Cache<String, TrackScrobble> processed = Caffeine.newBuilder()
            .maximumSize(500).build();


    public static <T>
    CompletableFuture<T> anyOf(List<? extends CompletionStage<? extends T>> l) {

        CompletableFuture<T> f = new CompletableFuture<>();
        Consumer<T> complete = f::complete;
        l.forEach(s -> s.thenAccept(complete));
        return f;
    }

    public TrackScrobble processScrobble(@Nullable Metadata metadata, AudioTrack song) {
        return processed.get(song.getIdentifier(), (s) -> newScrobble(metadata, song));
    }

    public TrackScrobble setMetadata(@NotNull Metadata metadata, @NotNull AudioTrack song, UUID previous, long current, long total) {
        TrackScrobble prev = processed.getIfPresent(song.getIdentifier());
        assert prev != null;

        TrackScrobble.DurationIndex index = prev.mapDuration(current, total);
        Processed intercepted = prev.processeds().get(index.index() - 1);
        var b = prev.withProcess(prev.processeds().stream().map(z -> z == intercepted ? new Processed(metadata.artist(), metadata.album(), metadata.song()) : z).toList());
        processed.put(song.getIdentifier(), b);
        return b;
    }

    public TrackScrobble setMetadata(@NotNull Metadata metadata, @NotNull AudioTrack song, UUID previous) {
        TrackScrobble newMapping = newScrobble(metadata, song);
        newMapping = new TrackScrobble(newMapping.scrobble(), newMapping.processeds(), song.getIdentifier(), previous);
        processed.put(song.getIdentifier(), newMapping);
        return newMapping;
    }

    public TrackScrobble newScrobble(@Nullable Metadata metadata, AudioTrack song) {
        AudioTrackInfo info = song.getInfo();
        InnerScrobble inn = new InnerScrobble(info.author, info.title, null, null, song.getDuration() != Long.MAX_VALUE ? song.getDuration() : null, new ArrayList<>());
        if (song instanceof MetadataTrack spo) {
            inn = inn.withAlbum(spo.getAlbum()).withImage(spo.getImage());
        } else if (info instanceof ChuuAudioTrackInfo chA) {
            inn = inn.fromChuu(chA);
        } else if (song instanceof YoutubeAudioTrack youtubeAudioTrack) {
            if (youtubeAudioTrack instanceof ChuuYoutubeAudioTrack cyat) {
                if (cyat.isSet()) {
                    ChuuAudioTrackInfo newInfo = cyat.newInfo;
                    inn = inn.fromChuu(newInfo);
                } else {
                    AudioTrackInfo process = cyat.process();
                    inn = inn.fromAudioTrack(cyat.getInfo());
                }
            }
        } else {
            inn = inn.withFilter();
        }
        if (inn.artist() != null && inn.song() != null && StringUtils.isBlank(inn.album())) {
            inn = processAlbum(inn, song);
        }
        return new TrackScrobble(inn
                .withMetadata(metadata), song.getIdentifier());
    }

    public void cleanScrobble(AudioTrack song) {
        processed.invalidate(song.getIdentifier());
    }

    private CompletableFuture<Album> transform(Supplier<Optional<Album>> source) {
        class TempException extends RuntimeException {
        }
        return CompletableFuture.supplyAsync(source).thenCompose(z -> {
            if (z.isEmpty()) {
                throw new TempException();
            } else
                return CompletableFuture.completedFuture(z.get());
        });
    }

    public InnerScrobble processAlbum(InnerScrobble innerScrobble, AudioTrack song) {

        if (!(song instanceof MetadataTrack)) {
            CompletableFuture<Album> cf1 = transform(() -> albumFinder.find(innerScrobble.artist(), innerScrobble.song()));
            CompletableFuture<Album> cf2 = transform(() -> albumFinder.findSpotify(innerScrobble.artist(), innerScrobble.song()));
            try {
                return anyOf(List.of(cf1, cf2))
                        .thenApply(z -> innerScrobble.withAlbum(z.albumName()).withImage(z.url()))
                        .exceptionally(err -> innerScrobble)
                        .get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Chuu.getLogger().warn("Timeout allegado " + e.getClass());
                return innerScrobble;
            }
        }
        return innerScrobble;
    }
}
