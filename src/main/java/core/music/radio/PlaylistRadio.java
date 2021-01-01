package core.music.radio;


import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.Chuu;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class PlaylistRadio implements RadioSource {
    private final String authorId;
    private final String name;

    public PlaylistRadio(String authorId, String name) {
        super();

        this.authorId = authorId;
        this.name = name;
    }

    @Override
    public String getName() {
        return null;
    }

    @Nullable
    @Override
    public CompletableFuture<AudioTrack> nextTrack(RadioTrackContext context) {
        CompletableFuture<Object> customPlaylist = Chuu.getDao().getCustomPlaylist(Long.parseLong(authorId), name);
        if (customPlaylist == null) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            Chuu.playerManager.decodeTrack("");
            return CompletableFuture.completedFuture(null);
        } catch (IOException exception) {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(2);
        writer.writeUTF(name);
        writer.writeUTF(authorId);
        writer.close();
    }
}
