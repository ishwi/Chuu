package core.music.radio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.Chuu;
import core.apis.spotify.SpotifyUtils;
import core.commands.utils.CommandUtil;
import dao.ChuuService;
import dao.everynoise.NoiseGenre;
import dao.everynoise.Release;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final record ReleaseRadio(String name, String genre, String uri, ChuuService db,
                                 Set<String> previousIdentifiers) implements RadioSource {
    public ReleaseRadio(String name, String genre, String uri, ChuuService db) {
        this(name, genre, uri, db, new HashSet<>());
    }

    public ReleaseRadio(String genre, String uri) {
        this("Releases", genre, uri, Chuu.getDb());
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
        writer.writeInt(5);
        writer.writeUTF(genre);
        writer.writeUTF(uri);
        writer.close(); // This invokes flush.
    }

    private CompletableFuture<AudioTrack> nextTrack0(RadioTrackContext context, AtomicInteger attempts) {
        if (attempts.get() > 3) {
            return CompletableFuture.completedFuture(null);
        }
        List<Release> releases = db.releasesOfGenre(new NoiseGenre(genre, null));

        if (releases.isEmpty()) {
            CompletableFuture<AudioTrack> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }
        int i = CommandUtil.rand.nextInt(releases.size());
        Release release = releases.get(i);

        var future = new CompletableFuture<AudioTrack>();
        Chuu.playerManager.loadItemOrdered(this, SpotifyUtils.getAlbumLink(release.uri()), RetriableLoader.getLoader(future, false, () -> new ReleaseRadioTrackContext(context, release, genre, uri), context, attempts, this::nextTrack0, previousIdentifiers));
        return future;
    }
}
