package core.music.utils;

import core.Chuu;
import core.music.radio.DiscordRadio;
import core.music.radio.RadioTrackContext;
import core.music.radio.RandomRadio;

import java.io.*;
import java.util.Objects;

public class TrackContext implements Serializable {
    private final long requester;
    private final long channelRequester;

    public String getChannelRequesterStr() {
        if (channelRequester != -1L)
            return "<#" + channelRequester + ">";
        else return "Unknown";
    }

    public String getRequesterStr() {
        if (requester != -1L) return "<@" + requester + ">";
        else return "Unknown";
    }

    public TrackContext(long requester, long channelRequester) {
        this.requester = requester;
        this.channelRequester = channelRequester;
    }

    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(1);
        // 1 => TrackContext
        // 2 => DiscordFMTrackContext
        // 3 => RadioTrackContext
        writer.writeLong(requester);
        writer.writeLong(channelRequester);
        writer.close();// This invokes flush.
    }

    public static TrackContext deserialize(ByteArrayInputStream stream) throws IOException {
        if (stream.available() == 0) {
            return null;
        }

        try {
            var reader = new DataInputStream(stream);
            var contextType = reader.readInt();
            var requester = reader.readLong();
            var requestedChannel = reader.readLong();

            var ctx = switch (contextType) {
                case 1 -> new TrackContext(requester, requestedChannel);
                case 2 -> {
                    var station = reader.readUTF();
                    //DiscordFMTrackContext(station, requester, requestedChannel)
                    yield new RadioTrackContext(requester, requestedChannel, new DiscordRadio(station));
                }
                case 3 -> {
                    var source = new DiscordRadio("temp").deserialize(stream);
                    yield new RadioTrackContext(requester, requestedChannel, source);
                }
                case 4 -> {
                    var source = new RandomRadio("Random Radio", null).deserialize(stream);
                    yield new RadioTrackContext(requester, requestedChannel, source);
                }
                default -> throw new IllegalArgumentException("Invalid contextType $contextType!");
            };

            reader.close();
            return ctx;
        } catch (EOFException e) {
            Chuu.getLogger().info("End of stream; no user data to be read! Remaining bytes: ${stream.available()}");
            return null;
        }
    }

    public long requester() {
        return requester;
    }

    public long channelRequester() {
        return channelRequester;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TrackContext) obj;
        return this.requester == that.requester &&
                this.channelRequester == that.channelRequester;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requester, channelRequester);
    }

    @Override
    public String toString() {
        return "TrackContext[" +
                "requester=" + requester + ", " +
                "channelRequester=" + channelRequester + ']';
    }

}
