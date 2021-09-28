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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package core.music.sources.attachments;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.*;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools.NoRedirectsStrategy;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoBuilder;
import core.music.utils.LimitedContainerRegistry;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.regex.Pattern;

public class DiscordAttachmentAudioSourceManager extends ProbingAudioSourceManager {
    private static final Pattern cdnRegex = Pattern.compile("^https?://cdn\\.discordapp\\.com/attachments/\\d{17,21}/\\d{17,21}/[a-zA-Z0-9_-]+\\.\\w{2,6}");
    private final HttpInterfaceManager httpInterfaceManager = new ThreadLocalHttpInterfaceManager(
            HttpClientTools
                    .createSharedCookiesHttpBuilder()
                    .setRedirectStrategy(new NoRedirectsStrategy()),
            HttpClientTools.DEFAULT_REQUEST_CONFIG
    );

    public DiscordAttachmentAudioSourceManager() {
        super(new LimitedContainerRegistry());
    }

    public HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }

    @Override
    public String getSourceName() {
        return "attachment";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (!(cdnRegex.matcher(reference.identifier).matches())) {
            return null;
        }

        if (reference.containerDescriptor != null) {
            return createTrack(AudioTrackInfoBuilder.create(reference, null).build(), reference.containerDescriptor);
        } else {
            return handleLoadResult(detectContainer(reference));
        }
    }


    private MediaContainerDetectionResult detectContainer(AudioReference reference) {
        try (var httpInterface = getHttpInterface()) {
            return detectContainerWithClient(httpInterface, reference);
        } catch (IOException e) {
            throw new FriendlyException("Connecting to the URL failed.", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    private MediaContainerDetectionResult detectContainerWithClient(HttpInterface httpInterface, AudioReference reference) {
        URI uri;
        try {
            uri = new URI(reference.identifier);
        } catch (URISyntaxException e) {
            throw new FriendlyException("Not a valid URL.", FriendlyException.Severity.COMMON, e);
        }


        // We could probably scrape content-length from headers.
        try (var inputStream = new PersistentHttpStream(httpInterface, uri, Units.CONTENT_LENGTH_UNKNOWN)) {
            int statusCode = inputStream.checkStatusCode();
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            } else if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new FriendlyException("That URL is not playable.", FriendlyException.Severity.COMMON, new IllegalStateException("Status code $statusCode"));
            }
            var hints = MediaContainerHints.from(getHeaderValue(inputStream.getCurrentResponse(), "Content-Type"), null);
            return new MediaContainerDetection(containerRegistry, reference, inputStream, hints).detectContainer();
        } catch (Exception e) {
            throw new FriendlyException("Exception reading data", FriendlyException.Severity.COMMON, e);
        }
    }

    private String getHeaderValue(HttpResponse response, String name) {
        return Optional.ofNullable(response.getFirstHeader(name)).map(NameValuePair::getValue).orElse(null);
    }


    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }


    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        MediaContainerDescriptor containerTrackFactory = decodeTrackFactory(input);
        if (containerTrackFactory == null) {
            return null;
        }
        return new DiscordAttachmentAudioTrack(trackInfo, containerTrackFactory, this);
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        encodeTrackFactory(((DiscordAttachmentAudioTrack) track).containerDescriptor(), output);
    }

    @Override
    protected AudioTrack createTrack(AudioTrackInfo trackInfo, MediaContainerDescriptor containerTrackFactory) {
        return new DiscordAttachmentAudioTrack(trackInfo, containerTrackFactory, this);
    }

    @Override
    public void shutdown() {
        // Do nothing.
    }


}
