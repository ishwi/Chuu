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
import dao.everynoise.NoiseGenre;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class GenreRadioTrackContext extends RadioTrackContext {
    private final String genre;
    private final String uri;
    private final int size;
    private final int index;

    public GenreRadioTrackContext(long requester, long channelRequester, RadioSource source, String genre, String uri, int size, int index) {
        super(requester, channelRequester, source);
        this.genre = genre;
        this.uri = uri;
        this.size = size;
        this.index = index;
    }

    public GenreRadioTrackContext(RadioTrackContext other, NoiseGenre genre, int size, int index) {
        super(other);
        this.genre = genre.name();
        this.uri = genre.uri();
        this.size = size;
        this.index = index;
    }


    public CompletableFuture<AudioTrack> nextTrack() {
        return getSource().nextTrack(this);
    }

    @Override
    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(6);
        // 1 => TrackContext
        // 2 => DiscordFMTrackContext
        // 3 => RadioTrackContext
        // 4 => RandomRadioTrackContext
        // 5 => GenreTrackContext
        writer.writeLong(requester());
        writer.writeLong(channelRequester());
        writer.writeUTF(genre);
        writer.writeUTF(uri);
        writer.writeInt(size);
        writer.writeInt(index);
        writer.close();// This invokes flush.
    }

    public String getGenre() {
        return genre;
    }

    public String getUri() {
        return uri;
    }

    public int getSize() {
        return size;
    }

    public int getIndex() {
        return index;
    }
}
