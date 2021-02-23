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
package core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.DecodedTrackHolder;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import com.sun.istack.Nullable;
import core.Chuu;
import core.music.sources.bandcamp.CustomBandcampAudioSourceManager;
import core.music.sources.spotify.SpotifyAudioSourceManager;
import core.music.utils.TrackContext;
import core.music.utils.YoutubeSearchManagerSingleton;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class ExtendedAudioPlayerManager extends DefaultAudioPlayerManager {
    private final String config = null;

    public ExtendedAudioPlayerManager() {
        super();
        AudioConfiguration configuration = getConfiguration();
        configuration.setFilterHotSwapEnabled(true);
        configuration.setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        YoutubeAudioSourceManager youtubeAudioSourceManager = YoutubeSearchManagerSingleton.getInstance();

        if (Chuu.ipv6Block != null && !Chuu.ipv6Block.isEmpty()) {
            @SuppressWarnings("rawtypes") List<IpBlock> blocks = List.of(new Ipv6Block(Chuu.ipv6Block));
            RotatingNanoIpRoutePlanner planner = new RotatingNanoIpRoutePlanner(blocks);
            new YoutubeIpRotatorSetup(planner).forSource(youtubeAudioSourceManager).setup();
        }
        registerSourceManagers(
                new SpotifyAudioSourceManager(youtubeAudioSourceManager),
                youtubeAudioSourceManager,
                SoundCloudAudioSourceManager.createDefault()
                , new GetyarnAudioSourceManager(),
                new CustomBandcampAudioSourceManager(),
                new VimeoAudioSourceManager(),
                new TwitchStreamAudioSourceManager(),
                new BeamAudioSourceManager(),
                new HttpAudioSourceManager()

        );


    }

    private void registerSourceManagers(AudioSourceManager... sourceManager) {
        for (AudioSourceManager audioSourceManager : sourceManager) {
            registerSourceManager(audioSourceManager);

        }
    }


    public String encodeTrack(AudioTrack track) throws IOException {
        var baos = new ByteArrayOutputStream();
        super.encodeTrack(new MessageOutput(baos), track);

        TrackContext trackContext = (TrackContext) track.getUserData();

        trackContext.serialize(baos); // Write our user data to the stream.

        var encoded = Base64.getEncoder().encodeToString(baos.toByteArray());
        baos.close();
        return encoded;
    }

    /**
     * @return An AudioTrack with possibly-null user data.
     */
    public @Nullable AudioTrack decodeTrack(String base64) throws IOException {
        var decoded = Base64.getDecoder().decode(base64);
        var bais = new ByteArrayInputStream(decoded);
        DecodedTrackHolder track = super.decodeTrack(new MessageInput(bais));

        AudioTrack audioTrack = track.decodedTrack;
        if (audioTrack == null) {
            return null;
        }
        TrackContext trackContext = TrackContext.deserialize(bais);

        if (trackContext != null) {
            audioTrack.setUserData(trackContext);
        }

        return audioTrack;
    }

    public BasicAudioPlaylist decodePlaylist(List<String> encodedTracks, String name) {
        List<AudioTrack> decoded = encodedTracks.stream().map(this::decodeMaybeNullAudioTrack).collect(Collectors.toList());
        return new BasicAudioPlaylist(name, decoded, decoded.get(0), false);
    }

    public String toJsonString(AudioPlaylist playlist) {
        AudioTrack selectedTrack = playlist.getSelectedTrack();
        int selectedIndex = playlist.getTracks().indexOf(selectedTrack);

        var tracks = new JSONArray();
        for (AudioTrack track : playlist.getTracks()) {
            var enc = encodeAudioTrack(track);
            tracks.put(enc);
        }
        JSONObject jsonObject = new JSONObject();
        return jsonObject.put("name", playlist.getName())
                .put("tracks", tracks)
                .put("search", playlist.isSearchResult())
                .put("selected", selectedIndex).toString();
    }

    public BasicAudioPlaylist decodePlaylist(String jsonString) {
        var jo = new JSONObject(jsonString);

        var name = jo.getString("name");
        var isSearch = jo.getBoolean("search");
        var selectedIndex = jo.getInt("selected");

        var encodedTracks = jo.getJSONArray("tracks");
        var tracks = new ArrayList<AudioTrack>();
        for (Object encodedTrack : encodedTracks) {
            var decodedTrack = decodeAudioTrack((String) encodedTrack);
            tracks.add(decodedTrack);

        }

        AudioTrack selectedTrack = null;
        if (selectedIndex > -1) {
            selectedTrack = tracks.get(selectedIndex);
        }
        return new BasicAudioPlaylist(name, tracks, selectedTrack, isSearch);
    }

    public List<String> encodePlaylist(BasicAudioPlaylist playlist) {
        return playlist.getTracks().stream().map(this::encodeAudioTrack).collect(Collectors.toList());
    }

    public String encodeAudioTrack(AudioTrack track) {
        try {
            return encodeTrack(track);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // This is used at the top of the file. Don't ask :^)
    public @Nullable AudioTrack decodeMaybeNullAudioTrack(String encoded) {
        try {
            return decodeTrack(encoded);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public AudioTrack decodeAudioTrack(String encoded) {


        try {
            return decodeTrack(encoded);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
