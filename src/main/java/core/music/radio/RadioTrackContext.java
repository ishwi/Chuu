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
        writer.writeInt(3);
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
