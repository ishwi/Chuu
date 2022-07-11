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
package core.music.sources.spotify.loaders;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import core.util.ChuuVirtualPool;
import se.michaelthelin.spotify.SpotifyApi;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Loader {

    protected final YoutubeAudioSourceManager youtubeAudioSourceManager;

    protected Loader(YoutubeAudioSourceManager youtubeAudioSourceManager) {
        this.youtubeAudioSourceManager = youtubeAudioSourceManager;
    }

    public AudioItem doYoutubeSearch(AudioPlayerManager manager, String identifier) {
        return youtubeAudioSourceManager.loadItem(manager, new AudioReference(identifier, null));
    }

    public abstract Pattern pattern();

    @Nullable
    public abstract AudioItem load(AudioPlayerManager manager, SpotifyApi spotifyApi, Matcher matcher);

    public CompletableFuture<AudioItem> queueYoutubeSearch(AudioPlayerManager manager, String identifier) {
        return CompletableFuture.supplyAsync(() -> youtubeAudioSourceManager.loadItem(manager, new AudioReference(identifier, null)), ChuuVirtualPool.of("Spotify-Loader"));
    }

    protected void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    record Metadata(String artist, String album, String song, String url) {
    }
}
