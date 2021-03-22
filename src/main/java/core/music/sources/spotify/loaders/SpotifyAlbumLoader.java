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
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import core.music.sources.spotify.SpotifyAudioSourceManager;
import dao.exceptions.ChuuServiceException;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpotifyAlbumLoader extends Loader {
    private final Pattern PLAYLIST_PATTERN = Pattern.compile("^(?:https?://(?:open\\.)?spotify\\.com|spotify)([/:])album\\1([a-zA-Z0-9]+)");
    private final SpotifyAudioSourceManager sourceManager;

    public SpotifyAlbumLoader(YoutubeAudioSourceManager youtubeAudioSourceManager, SpotifyAudioSourceManager sourceManager) {
        super(youtubeAudioSourceManager);
        this.sourceManager = sourceManager;
    }


    @Override
    public Pattern pattern() {
        return PLAYLIST_PATTERN;
    }

    @Nullable
    @Override
    public AudioItem load(AudioPlayerManager manager, SpotifyApi spotifyApi, Matcher matcher) {
        var albumId = matcher.group(2);

        Album execute;
        try {
            execute = spotifyApi.getAlbum(albumId).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new ChuuServiceException(exception);
        }
        TrackSimplified[] items = execute.getTracks().getItems();
        check(items.length != 0, "Album " + albumId + " is missing track items!");
        String name = execute.getName();
        var albumName = name == null || name.isBlank() ? "Untitled Album" : name;

        List<AudioTrack> audioTracks = fetchAlbumTracks(manager, spotifyApi, execute);
        return new BasicAudioPlaylist(albumName, audioTracks, null, false);
    }

    private List<AudioTrack> fetchAlbumTracks(AudioPlayerManager manager,
                                              SpotifyApi spotifyApi, Album album) {
        var tasks = new ArrayList<CompletableFuture<AudioTrack>>();
        String name = album.getName() == null || album.getName().isBlank() ? "Untitled Album" : album.getName();
        String url = Arrays.stream(album.getImages()).max(Comparator.comparingInt((Image x) -> x.getHeight() * x.getWidth())).map(Image::getUrl).orElse(null);
        for (TrackSimplified trackSimplified : album.getTracks().getItems()) {
            String title = trackSimplified.getName();
            String artist = trackSimplified.getArtists()[0].getName();

            CompletableFuture<AudioTrack> task = queueYoutubeSearch(manager, "ytsearch:" + title + " " + artist).thenApply(ai -> {
                if (ai instanceof AudioPlaylist ap) {
                    return new SpotifyAudioTrack((YoutubeAudioTrack) ap.getTracks().get(0), artist, name, title, url, this.sourceManager);
                } else {
                    return new SpotifyAudioTrack((YoutubeAudioTrack) ai, artist, name, title, url, this.sourceManager);
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
        }).filter(Objects::nonNull).toList();
    }

}
