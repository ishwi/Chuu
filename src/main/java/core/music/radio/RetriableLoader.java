package core.music.radio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.commands.utils.CommandUtil;
import core.music.utils.TrackContext;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RetriableLoader {
    public static AudioLoadResultHandler getLoader(CompletableFuture<AudioTrack> future, boolean randomizePlaylist, Supplier<TrackContext> contextSupplier, RadioTrackContext context, AtomicInteger attempts, BiFunction<RadioTrackContext, AtomicInteger, CompletableFuture<AudioTrack>> retrier, Set<String> previousIdentifiers) {
        return getLoader(future, randomizePlaylist, contextSupplier, context, attempts, retrier, previousIdentifiers, null);
    }

    public static AudioLoadResultHandler getLoader(CompletableFuture<AudioTrack> future, boolean randomizePlaylist, Supplier<TrackContext> contextSupplier, RadioTrackContext context, AtomicInteger attempts, BiFunction<RadioTrackContext, AtomicInteger, CompletableFuture<AudioTrack>> retrier, Set<String> previousIdentifiers, AtomicReference<AudioPlaylist> ref) {
        return new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(contextSupplier.get());
                future.complete(track);
                previousIdentifiers.add(track.getIdentifier());
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack track;
                int counter = 0;
                if (ref != null) {
                    ref.compareAndExchange(null, playlist);
                }
                List<AudioTrack> tracks = playlist.getTracks();
                do {

                    tracks = tracks.stream().filter(z -> !previousIdentifiers.contains(z.getIdentifier())).toList();
                    if (tracks.isEmpty()) {
                        loadFailed(null);
                        return;
                    }
                    if (randomizePlaylist) {
                        track = tracks.get(CommandUtil.rand.nextInt(tracks.size()));
                    } else {
                        track = tracks.get(0);
                    }
                } while (previousIdentifiers.contains(track.getIdentifier()) && !tracks.isEmpty());


                trackLoaded(track);
            }

            @Override
            public void noMatches() {
                future.complete(null);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (attempts.get() >= 3) {
                    future.obtrudeException(new FriendlyException("", FriendlyException.Severity.COMMON, null));
                } else {
                    int i = attempts.incrementAndGet();
                    retrier.apply(context, attempts)
                            .thenAccept(future::complete);
                }
            }
        };
    }


}
