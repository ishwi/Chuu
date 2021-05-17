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

import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dao.everynoise.Release;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ReleaseRadioTrackContext extends RadioTrackContext {
    private final String release;
    private final String artist;
    private final String uri;
    private final String genre;
    private final String genreUri;

    public ReleaseRadioTrackContext(long requester, long channelRequester, RadioSource source, String release, String artist, String uri, String genre, String genreUri) {
        super(requester, channelRequester, source);
        this.release = release;
        this.artist = artist;
        this.uri = uri;
        this.genre = genre;
        this.genreUri = genreUri;
    }

    public ReleaseRadioTrackContext(RadioTrackContext other, Release release, String genre, String genreUri) {
        super(other);
        this.release = release.release();
        this.artist = release.artist();
        this.uri = release.uri();
        this.genre = genre;
        this.genreUri = genreUri;

    }


    public CompletableFuture<AudioTrack> nextTrack() {
        return getSource().nextTrack(this);
    }

    public void serialize(ByteArrayOutputStream stream) throws IOException {
        var writer = new DataOutputStream(stream);
        writer.writeInt(5);
        // 1 => TrackContext
        // 2 => DiscordFMTrackContext
        // 3 => RadioTrackContext
        // 4 => RandomRadioTrackContext
        // 5 => GenreTrackContext
        writer.writeLong(requester());
        writer.writeLong(channelRequester());
        writer.writeUTF(genre);
        writer.writeUTF(genreUri);
        DataFormatTools.writeNullableText(writer, release);
        DataFormatTools.writeNullableText(writer, artist);
        DataFormatTools.writeNullableText(writer, uri);
        writer.close();// This invokes flush.
    }

    public String getRelease() {
        return release;
    }

    public String getArtist() {
        return artist;
    }

    public String getUri() {
        return uri;
    }

    public String getGenre() {
        return genre;
    }

    public String getGenreUri() {
        return genreUri;
    }
}
