package core.music.radio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.Chuu;
import core.apis.spotify.SpotifyUtils;
import dao.ChuuService;
import dao.everynoise.NoiseGenre;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class GenreRadio implements RadioSource {
    private final String name;
    private final String genre;
    private final String uri;
    private final ChuuService db;
    private final Set<String> previousIdentifier;
    private final AtomicReference<AudioPlaylist> ref = new AtomicReference<>();
    private int songCount = -1;
    private int index = 1;


    public GenreRadio(String name, String genre, String uri, ChuuService db,
                      Set<String> previousIdentifier) {
        this.name = name;
        this.genre = genre;
        this.uri = uri;
        this.db = db;
        this.previousIdentifier = previousIdentifier;
    }

    public GenreRadio(String name, String genre, String uri, ChuuService db) {
        this(name, genre, uri, db, new HashSet<>());
    }

    public GenreRadio(String genre, String uri) {
        this("everynoise genres", genre, uri, Chuu.getDb());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CompletableFuture<AudioTrack> nextTrack(RadioTrackContext context) {

        return this.nextTrack0(context, new AtomicInteger(1));
    }

    @Override
    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(6);
        writer.writeUTF(genre);
        writer.writeUTF(uri);
        writer.close(); // This invokes flush.
    }

    private CompletableFuture<AudioTrack> nextTrack0(RadioTrackContext context, AtomicInteger attempts) {
        if (attempts.get() > 3) {
            return CompletableFuture.completedFuture(null);
        }
        Optional<NoiseGenre> matchingGenre = db.findExactMatch(genre);


        if (matchingGenre.isEmpty()) {
            CompletableFuture<AudioTrack> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }

        NoiseGenre genre = matchingGenre.get();

        var future = new CompletableFuture<AudioTrack>();
        AudioLoadResultHandler loader = RetriableLoader.getLoader(future, true,
                () -> {
                    AudioPlaylist audioPlaylist = this.ref.get();
                    this.songCount = audioPlaylist != null ? audioPlaylist.getTracks().size() : -1;
                    return new GenreRadioTrackContext(context, genre, songCount, this.index++);
                }, context, attempts, this::nextTrack0, this.previousIdentifier, ref);
        if (this.ref.get() != null) {
            loader.playlistLoaded(this.ref.get());
        } else {
            Chuu.playerManager.loadItemOrdered(this, SpotifyUtils.getPlaylistLink(genre.uri()), loader);
        }
        return future;
    }

    public String name() {
        return name;
    }

    public String genre() {
        return genre;
    }

    public String uri() {
        return uri;
    }

    public ChuuService db() {
        return db;
    }

    public Set<String> previousIdentifier() {
        return previousIdentifier;
    }


}
