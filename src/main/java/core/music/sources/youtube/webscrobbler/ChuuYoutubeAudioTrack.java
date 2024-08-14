package core.music.sources.youtube.webscrobbler.processers;

import com.sedmelluq.discord.lavaplayer.container.matroska.MatroskaAudioTrack;
import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeClientConfig;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeConstants;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeFormatInfo;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeMpegStreamAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubePersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackDetails;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackFormat;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import core.Chuu;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.sedmelluq.discord.lavaplayer.container.Formats.MIME_AUDIO_WEBM;
import static com.sedmelluq.discord.lavaplayer.tools.DataFormatTools.decodeUrlEncodedItems;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.Units.CONTENT_LENGTH_UNKNOWN;


public class ChuuYoutubeAudioTrack extends YoutubeAudioTrack {
    private static final Logger log = Chuu.getLogger();
    private final YoutubeAudioSourceManager sourceManager;
    private final Lock readLock;
    private final Lock writeLock;
    public ChuuAudioTrackInfo newInfo;
    private FormatWithUrl cachedFormatWithUrl;

    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    /**
     * @param trackInfo     Track info
     * @param sourceManager Source manager which was used to find this track
     */

    public ChuuYoutubeAudioTrack(AudioTrackInfo trackInfo, YoutubeAudioSourceManager sourceManager) {
        super(trackInfo, sourceManager);
        this.sourceManager = sourceManager;
    }

    private static boolean isBetterFormat(YoutubeTrackFormat format, YoutubeTrackFormat other) {
        YoutubeFormatInfo info = format.getInfo();

        if (info == null) {
            return false;
        } else if (other == null) {
            return true;
        } else if (MIME_AUDIO_WEBM.equals(info.mimeType) && format.getAudioChannels() > 2) {
            // Opus with more than 2 audio channels is unsupported by LavaPlayer currently.
            return false;
        } else if (info.ordinal() != other.getInfo().ordinal()) {
            return info.ordinal() < other.getInfo().ordinal();
        } else {
            return format.getBitrate() > other.getBitrate();
        }
    }

    private static YoutubeTrackFormat findBestSupportedFormat(List<YoutubeTrackFormat> formats) {
        YoutubeTrackFormat bestFormat = null;

        for (YoutubeTrackFormat format : formats) {
            if (!format.isDefaultAudioTrack()) {
                continue;
            }

            if (isBetterFormat(format, bestFormat)) {
                bestFormat = format;
            }
        }

        if (bestFormat == null) {
            StringJoiner joiner = new StringJoiner(", ");
            formats.forEach(format -> joiner.add(format.getType().toString()));
            throw new IllegalStateException("No supported audio streams available, available types: " + joiner);
        }

        return bestFormat;
    }

    public boolean isSet() {
        try {
            readLock.lock();
            return cachedFormatWithUrl != null;
        } finally {
            readLock.unlock();
        }
    }

    public AudioTrackInfo processInfo() throws Exception {
        if (!trackInfo.isStream) {
            try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
                loadBestFormatWithUrl(null);
            } catch (Exception e) {
                Chuu.getLogger().info("Something went wrong loading a track in chuu interceptor: " + e.getMessage(), e);
                throw e;
            }


        }
        return getInfo();
    }

    @Override
    public AudioTrackInfo getInfo() {
        if (this.newInfo == null) {
            return super.getInfo();
        }
        return newInfo;
    }

    private FormatWithUrl loadBestFormatWithUrl(YoutubeClientConfig config) throws Exception {
        try {
            writeLock.lock();
            if (this.cachedFormatWithUrl != null) {
                return this.cachedFormatWithUrl;
            }
            try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
                YoutubeTrackDetails details = sourceManager.getTrackDetailsLoader()
                        .loadDetails(httpInterface, getIdentifier(), true, sourceManager, config);
                // If the error reason is "Video unavailable" details will return null
                if (details == null) {
                    throw new FriendlyException("This video is not available", FriendlyException.Severity.COMMON, null);
                }

                List<YoutubeTrackFormat> formats = details.getFormats(httpInterface, sourceManager.getSignatureResolver());

                YoutubeTrackFormat format = findBestSupportedFormat(formats);

                URI signedUrl = sourceManager.getSignatureResolver()
                        .resolveFormatUrl(httpInterface, details.getPlayerScript(), format);

                var formatWithUrl = new FormatWithUrl(format, signedUrl, details.getPlayerScript());
                setCachedInfo((ChuuAudioTrackInfo) details.getTrackInfo(), formatWithUrl);
                return formatWithUrl;
            }
        } finally {
            writeLock.unlock();

        }
    }

    private void setCachedInfo(ChuuAudioTrackInfo newInfo, FormatWithUrl details) {
        if (this.cachedFormatWithUrl == null) {
            this.newInfo = newInfo;
            this.cachedFormatWithUrl = details;
        } else {
            Chuu.getLogger().warn("Concurrency problem while intercepting track?? {} ", newInfo.identifier);
        }
    }

    private void processStream(LocalAudioTrackExecutor localExecutor, FormatWithUrl format) throws Exception {
        if (MIME_AUDIO_WEBM.equals(format.details.getType().getMimeType())) {
            throw new FriendlyException("YouTube WebM streams are currently not supported.", COMMON, null);
        }

        try (HttpInterface streamingInterface = sourceManager.getHttpInterface()) {
            processDelegate(new YoutubeMpegStreamAudioTrack(trackInfo, streamingInterface, format.signedUrl), localExecutor);
        }
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        if (trackInfo.isStream) {
            super.process(localExecutor);
        } else {
            FormatWithUrl format;
            readLock.lock();
            if (this.cachedFormatWithUrl != null) {
                readLock.unlock();
                format = cachedFormatWithUrl;
            } else {
                readLock.unlock();
                format = loadBestFormatWithUrl(null);
                // first attempt with ANDROID client
                log.debug("Starting track from URL: {}", format.signedUrl);

                if (format.details.getContentLength() == CONTENT_LENGTH_UNKNOWN) {
                    processStream(localExecutor, format); // perhaps this should be using the interface too?
                } else {
                    try {
                        processStatic(localExecutor, format);
                    } catch (RuntimeException e) {
                        String message = e.getMessage();

                        if (!"Not success status code: 403".equals(message) && !"Invalid status code for video page response: 400".equals(message)) {
                            throw e;
                        }

                        String code = message.split(": ", 2)[1];

                        log.warn("Encountered {} when requesting formats with default client, re-requesting with WEB client.", code);

                        YoutubeClientConfig fallbackConfig = YoutubeClientConfig.WEB.copy()
                                .withRootField("params", YoutubeConstants.PLAYER_PARAMS_WEB);

                        format = loadBestFormatWithUrl(fallbackConfig);
                        processStatic(localExecutor, format);
                    }
                }
            }
        }
    }

    private void processStatic(LocalAudioTrackExecutor localExecutor, FormatWithUrl format) throws Exception {
        try (HttpInterface httpInterface = sourceManager.getHttpInterface();
             YoutubePersistentHttpStream stream = new YoutubePersistentHttpStream(httpInterface, format.signedUrl, format.details.getContentLength())) {

            if (format.details.getType().getMimeType().endsWith("/webm")) {
                processDelegate(new MatroskaAudioTrack(trackInfo, stream), localExecutor);
            } else {
                processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
            }
        }
    }

    private record FormatWithUrl(YoutubeTrackFormat details,
                                 URI signedUrl, String playerScriptUrl) {

        public FormatWithUrl getFallback() {
            String signedUrl = this.signedUrl.toString();
            Map<String, String> urlParameters = decodeUrlEncodedItems(signedUrl, false);

            String mn = urlParameters.get("mn");

            if (mn == null) {
                return null;
            }

            String[] hosts = mn.split(",");

            if (hosts.length < 2) {
                log.warn("Cannot fallback, available hosts: {}", String.join(", ", hosts));
                return null;
            }

            String newUrl = signedUrl.replaceFirst(hosts[0], hosts[1]);

            try {
                URI uri = new URI(newUrl);
                return new FormatWithUrl(details, uri, playerScriptUrl);
            } catch (URISyntaxException e) {
                return null;
            }
        }
    }

}
