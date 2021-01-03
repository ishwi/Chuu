package core.music.sources.bandcamp;

import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import core.music.sources.MetadataTrack;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class CustomBandcampAudioTrack extends DelegatedAudioTrack implements MetadataTrack {
    private static final Logger log = LoggerFactory.getLogger(BandcampAudioTrack.class);
    private final String album;
    private final String image;
    private final CustomBandcampAudioSourceManager sourceManager;

    /**
     * @param trackInfo     Track info
     * @param sourceManager Source manager which was used to find this track
     */
    public CustomBandcampAudioTrack(AudioTrackInfo trackInfo, CustomBandcampAudioSourceManager sourceManager, String album, String image) {
        super(trackInfo);
        this.album = album;
        this.image = image;
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
            log.debug("Loading Bandcamp track page from URL: {}", trackInfo.identifier);

            String trackMediaUrl = getTrackMediaUrl(httpInterface);
            log.debug("Starting Bandcamp track from URL: {}", trackMediaUrl);

            try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(trackMediaUrl), null)) {
                processDelegate(new Mp3AudioTrack(trackInfo, stream), localExecutor);
            }
        }
    }

    private String getTrackMediaUrl(HttpInterface httpInterface) throws IOException {
        try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(trackInfo.identifier))) {
            HttpClientTools.assertSuccessWithContent(response, "track page");

            String responseText = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonBrowser trackInfo = sourceManager.readTrackListInformation(responseText);

            return trackInfo.get("trackinfo").index(0).get("file").get("mp3-128").text();
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new CustomBandcampAudioTrack(trackInfo, sourceManager, album, image);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }

    @Override
    public String getAlbum() {
        return album;
    }


    @Override
    public String getImage() {
        return image;
    }


}
