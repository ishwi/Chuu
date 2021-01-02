package core.music.sources.spotify.loaders;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.wrapper.spotify.SpotifyApi;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Loader {

    protected final YoutubeAudioSourceManager youtubeAudioSourceManager;

    protected Loader(YoutubeAudioSourceManager youtubeAudioSourceManager) {
        this.youtubeAudioSourceManager = youtubeAudioSourceManager;
    }

    public abstract Pattern pattern();

    @Nullable
    public abstract AudioItem load(DefaultAudioPlayerManager manager, SpotifyApi spotifyApi, Matcher matcher);

    public CompletableFuture<AudioItem> queueYoutubeSearch(DefaultAudioPlayerManager manager, String identifier) {
        return CompletableFuture.supplyAsync(() -> youtubeAudioSourceManager.loadItem(manager, new AudioReference(identifier, null)));
    }

    public AudioItem doYoutubeSearch(DefaultAudioPlayerManager manager, String identifier) {
        return youtubeAudioSourceManager.loadItem(manager, new AudioReference(identifier, null));
    }

    protected void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
