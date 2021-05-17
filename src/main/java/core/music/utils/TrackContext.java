/*
 * MIT License
 *
 * Copyright (c) 2020 Melms Media LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 From Octave bot https://github.com/Stardust-Discord/Octave/ Modified for integrating with JAVA and the current bot
 */
package core.music.utils;

import core.Chuu;
import core.music.radio.*;

import java.io.*;
import java.util.Objects;

public class TrackContext implements Serializable {
    private final long requester;
    private final long channelRequester;

    public TrackContext(long requester, long channelRequester) {
        this.requester = requester;
        this.channelRequester = channelRequester;
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
                    var source = RadioSource.deserialize(stream);
                    yield new RadioTrackContext(requester, requestedChannel, source);
                }
                case 4 -> {
                    var source = new RandomRadio(reader.readLong(), reader.readBoolean());
                    yield new RandomRadioTrackContext(requester, requestedChannel, source, reader.readLong(), reader.readUTF());
                }
                case 5 -> {
                    ReleaseRadio releaseRadio = new ReleaseRadio(reader.readUTF(), reader.readUTF());
                    yield new ReleaseRadioTrackContext(requester, requestedChannel, releaseRadio, reader.readUTF(), reader.readUTF(), reader.readUTF(), releaseRadio.genre(), releaseRadio.uri());
                }
                case 6 -> {
                    var source = new GenreRadio(reader.readUTF(), reader.readUTF());
                    yield new GenreRadioTrackContext(requester, requestedChannel, source, source.genre(), source.uri(), reader.readInt(), reader.readInt());
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

    public String getChannelRequesterStr() {
        if (channelRequester != -1L)
            return "<#" + channelRequester + ">";
        else return "Unknown";
    }

    public String getRequesterStr() {
        if (requester != -1L) return "<@" + requester + ">";
        else return "Unknown";
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
