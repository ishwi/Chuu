package core.music.radio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.music.utils.TrackContext;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RadioTrackContext extends TrackContext {
    private final RadioSource source;


    public RadioTrackContext(long requester, long channelRequester, RadioSource source) {
        super(requester, channelRequester);
        this.source = source;
    }

    public CompletableFuture<AudioTrack> nextTrack() {
        return source.nextTrack(this);
    }

    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(1);
        // 1 => TrackContext
        // 2 => DiscordFMTrackContext
        // 3 => RadioTrackContext
        writer.writeLong(requester());
        writer.writeLong(channelRequester());
        writer.close();// This invokes flush.
    }

    public RadioSource getSource() {
        return source;
    }
}
