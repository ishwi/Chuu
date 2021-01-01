package core.music.radio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface RadioSource {
    String getName();

    @Nullable
    CompletableFuture<AudioTrack> nextTrack(RadioTrackContext context);

    void serialize(ByteArrayOutputStream stream) throws IOException;

    default RadioSource deserialize(ByteArrayInputStream stream) throws IOException {
        if (stream.available() == 0) {
            throw new IllegalStateException("Cannot parse RadioSource with no remaining bytes");
        }

        var reader = new DataInputStream(stream);

        RadioSource ctx = switch (reader.readInt()) {
            case 1 -> new DiscordRadio(reader.readUTF());
            case 2 -> new PlaylistRadio(reader.readUTF(), reader.readUTF());
            default -> throw new IllegalArgumentException("Invalid contextType $sourceType!");
        };

        reader.close();
        return ctx;
    }
}
