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
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import dao.exceptions.ChuuServiceException;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class SpotifyAlbumLoader extends Loader {
    private final Pattern PLAYLIST_PATTERN = Pattern.compile("^(?:https?://(?:open\\.)?spotify\\.com|spotify)([/:])album\\1([a-zA-Z0-9]+)");

    public SpotifyAlbumLoader(YoutubeAudioSourceManager youtubeAudioSourceManager) {
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

        Album execute;
        try {
            execute = spotifyApi.getAlbum(albumId).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new ChuuServiceException(exception);
        }
        TrackSimplified[] items = execute.getTracks().getItems();
        check(items.length == 0, "Album $albumId is missing track items!");
        List<AudioTrack> audioTracks = fetchAlbumTracks(manager, spotifyApi, items);
        String name = execute.getName();
        var albumName = name == null || name.isBlank() ? "Untitled Album" : name;

        return new BasicAudioPlaylist(albumName, audioTracks, null, false);
    }

    private List<AudioTrack> fetchAlbumTracks(DefaultAudioPlayerManager manager,
                                              SpotifyApi spotifyApi, TrackSimplified[] track) {
        var tasks = new ArrayList<CompletableFuture<AudioTrack>>();
        for (TrackSimplified trackSimplified : track) {
            String title = trackSimplified.getName();
            String artist = trackSimplified.getArtists()[0].getName();
            CompletableFuture<AudioTrack> task = queueYoutubeSearch(manager, "ytsearch:" + title + " " + artist).thenApply(ai -> {
                if (ai instanceof AudioPlaylist ap) {
                    return ap.getTracks().get(0);
                } else {
                    return (AudioTrack) ai;
                }
            });
            tasks.add(task);
        }
        try {
            CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).get();
        } catch (Exception ignored) {
        }

        return tasks.stream().filter(t -> !t.isCompletedExceptionally()).map(x -> {
            try {
                return x.get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
