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
package core.music.sources.spotify;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.wrapper.spotify.SpotifyApi;
import core.apis.spotify.SpotifySingleton;
import core.music.sources.spotify.loaders.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public class SpotifyAudioSourceManager implements AudioSourceManager {
    private final SpotifyApi spotifyApi;
    private final List<Loader> loaders;
    private final YoutubeAudioSourceManager yt;

    public SpotifyAudioSourceManager(YoutubeAudioSourceManager yt) {
        this.yt = yt;
        this.spotifyApi = SpotifySingleton.getInstance().getSpotifyApi();
        loaders = List.of(new SpotifyTrackLoader(yt, this), new SpotifyPlaylistLoader(yt, this), new SpotifyAlbumLoader(yt, this));

    }

    @Override
    public String getSourceName() {
        return "Spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        try {
            return loadItemOnce(manager, reference.identifier);
        } catch (FriendlyException exception) {
            // In case of a connection reset exception, try once more.
            if (HttpClientTools.isRetriableNetworkException(exception.getCause())) {
                return loadItemOnce(manager, reference.identifier);
            } else {
                throw exception;
            }
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        SpotifyAudioTrack sp = (SpotifyAudioTrack) track;
        DataFormatTools.writeNullableText(output, sp.getAlbum());
        DataFormatTools.writeNullableText(output, sp.getImage());
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        YoutubeAudioTrack baseAudioTack = new YoutubeAudioTrack(trackInfo, yt);
        String album = DataFormatTools.readNullableText(input);
        String image = DataFormatTools.readNullableText(input);

        return new SpotifyAudioTrack(baseAudioTack, baseAudioTack.getInfo().author, album, baseAudioTack.getInfo().title, image, this);
    }

    @Override
    public void shutdown() {

    }

    private AudioItem loadItemOnce(AudioPlayerManager manager, String identifier) {
        for (var loader : loaders) {
            var matcher = loader.pattern().matcher(identifier);

            if (matcher.find()) {
                return loader.load(manager, this.spotifyApi, matcher);
            }
        }

        return null;
    }
}
