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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RandomRadioTrackContext extends RadioTrackContext {
    private final RandomRadio source;
    private final String uri;
    private final long currentSourcer;

    public RandomRadioTrackContext(RadioTrackContext other, RandomRadio source, String uri, long currentSourcer) {
        super(other);
        this.source = source;
        this.uri = uri;
        this.currentSourcer = currentSourcer;
    }

    public RandomRadioTrackContext(long requester, long channelRequester, RadioSource source, long currentSourcer, String currrentUri) {
        super(requester, channelRequester, source);
        this.source = (RandomRadio) source;
        this.currentSourcer = currentSourcer;
        this.uri = currrentUri;
    }

    public CompletableFuture<AudioTrack> nextTrack() {
        return source.nextTrack(this);
    }

    @Override
    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(4);
        // 1 => TrackContext
        // 2 => DiscordFMTrackContext
        // 3 => RadioTrackContext
        // 4 => RandomRadioTrackContext
        writer.writeLong(requester());
        writer.writeLong(channelRequester());
        writer.writeLong(source.guildId());
        writer.writeBoolean(source.onlyServer());
        writer.writeLong(currentSourcer);
        writer.writeUTF(uri);
        writer.close();// This invokes flush.
    }


    public String getUri() {
        return uri;
    }

    public long getCurrentSourcer() {
        return currentSourcer;
    }

}
