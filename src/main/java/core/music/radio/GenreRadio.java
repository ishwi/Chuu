package core.music.radio;

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

public final record GenreRadio(String name, String genre, String uri, ChuuService db,
                               Set<String> previousIdentifier) implements RadioSource {

    public GenreRadio(String name, String genre, String uri, ChuuService db) {
        this(name, genre, uri, db, new HashSet<>());
    }

    public GenreRadio(String genre, String uri) {
        this("EveryNoise genres", genre, uri, Chuu.getDb());
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
        Chuu.playerManager.loadItemOrdered(this, SpotifyUtils.getPlaylistLink(genre.uri()), RetriableLoader.getLoader(future, true,
                () -> new GenreRadioTrackContext(context, genre), context, attempts, this::nextTrack0, this.previousIdentifier));
        return future;
    }
}
