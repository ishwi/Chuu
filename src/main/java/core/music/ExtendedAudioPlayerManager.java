package core.music;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
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
import com.sun.istack.Nullable;
import core.music.sources.spotify.SpotifyAudioSourceManager;
import core.music.utils.TrackContext;
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
        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(true);
//        if (!config.isEmpty()) {
//            List<IpBlock> blocks;
//            blocks = List.of(new Ipv6Block(config));
//            AbstractRoutePlanner planner = null;
//            if (
//                    config.isEmpty()) {
//                new NanoIpRoutePlanner()
//                planner = new RotatingNanoIpRoutePlanner(blocks);
//                new
//            } else {
//                try {
//                    var blacklistedGW = InetAddress.getByName("");
//                    planner = new RotatingNanoIpRoutePlanner(blocks);
//
//                } catch (Exception ex) {
//                    planner = new RotatingNanoIpRoutePlanner(blocks);
//                }
//            }
//
//
//        }
        registerSourceManagers(
                new SpotifyAudioSourceManager(youtubeAudioSourceManager),
                youtubeAudioSourceManager,
                SoundCloudAudioSourceManager.createDefault()
                , new GetyarnAudioSourceManager(),
                new BandcampAudioSourceManager(),
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
