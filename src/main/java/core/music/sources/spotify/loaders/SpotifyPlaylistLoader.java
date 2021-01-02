package core.music.sources.spotify.loaders;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.IPlaylistItem;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import dao.exceptions.ChuuServiceException;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class SpotifyPlaylistLoader extends Loader {
    private static final String URL_PATTERN = "https?://(?:open\\.)?spotify\\.com(?:/user/[a-zA-Z0-9_]+)?";
    private static final Pattern PLAYLIST_PATTERN = Pattern.compile("^(?:" + URL_PATTERN + "|spotify)([/:])playlist\\1([a-zA-Z0-9]+)");

    public SpotifyPlaylistLoader(YoutubeAudioSourceManager youtubeAudioSourceManager) {
        super(youtubeAudioSourceManager);
    }


    @Override
    public Pattern pattern() {
        return PLAYLIST_PATTERN;
    }

    @Nullable
    @Override
    public AudioItem load(DefaultAudioPlayerManager manager, SpotifyApi spotifyApi, Matcher matcher) {
        var playlistId = matcher.group(2);

        Playlist execute;
        PlaylistTrack[] b;
        try {
            execute = spotifyApi.getPlaylist(playlistId).build().execute();
            b = spotifyApi.getPlaylistsItems(playlistId).build().execute().getItems();
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new ChuuServiceException(exception);
        }
        PlaylistTrack[] items = execute.getTracks().getItems();

        check(items.length == 0, "Album $albumId is missing track items!");
        List<AudioTrack> audioTracks = fetchAlbumTracks(manager, spotifyApi, b);
        String name = execute.getName();
        var albumName = name == null || name.isBlank() ? "Untitled Album" : name;

        return new BasicAudioPlaylist(albumName, audioTracks, null, false);
    }

    private List<AudioTrack> fetchAlbumTracks(DefaultAudioPlayerManager manager,
                                              SpotifyApi spotifyApi, PlaylistTrack[] track) {
        var tasks = new ArrayList<CompletableFuture<AudioTrack>>();
        for (PlaylistTrack plTrack : track) {
            IPlaylistItem track1 = plTrack.getTrack();
            if (track1 instanceof Track tr) {
                String name = tr.getName();
                String artistName = tr.getArtists()[0].getName();
                CompletableFuture<AudioTrack> task = queueYoutubeSearch(manager, "ytsearch:" + name + " " + artistName).thenApply(ai -> {
                    if (ai instanceof AudioPlaylist ap) {
                        return ap.getTracks().get(0);
                    } else {
                        return (AudioTrack) ai;
                    }
                });
                tasks.add(task);
            }
        }
        try {
            CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).get();
        } catch (Exception ignored) {
        }

        return tasks.stream().filter(t -> !t.isCompletedExceptionally()).map(x -> {
            try {
                return x.get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
