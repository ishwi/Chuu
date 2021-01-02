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

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Track;
import dao.exceptions.ChuuServiceException;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpotifyTrackLoader extends Loader {
    private final Pattern PLAYLIST_PATTERN = Pattern.compile("^(?:https?://(?:open\\.)?spotify\\.com|spotify)([/:])track\\1([a-zA-Z0-9]+)");

    public SpotifyTrackLoader(YoutubeAudioSourceManager youtubeAudioSourceManager) {
        super(youtubeAudioSourceManager);
    }


    @Override
    public Pattern pattern() {
        return PLAYLIST_PATTERN;
    }

    @Nullable
    @Override
    public AudioItem load(DefaultAudioPlayerManager manager, SpotifyApi spotifyApi, Matcher matcher) {
        var albumId = matcher.group(2);

        Track execute;
        try {
            execute = spotifyApi.getTrack(albumId).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new ChuuServiceException(exception);
        }
        String song = execute.getName();
        String artist = execute.getArtists()[0].getName();
        return doYoutubeSearch(manager, "ytsearch:" + song + " " + artist);
    }

}
