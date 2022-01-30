package core.music.sources.youtube.webscrobbler;

import com.sedmelluq.discord.lavaplayer.container.matroska.MatroskaAudioTrack;
import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.*;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import core.Chuu;
import core.music.sources.youtube.webscrobbler.processers.ChuuAudioTrackInfo;

import java.net.URI;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChuuYoutubeAudioTrack extends YoutubeAudioTrack {

    private final YoutubeAudioSourceManager sourceManager;
    public ChuuAudioTrackInfo newInfo;
    private FormatWithUrl cachedFormatWithUrl;
    private final Lock readLock;
    private final Lock writeLock;

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

    private static YoutubeTrackFormat findBestSupportedFormat(List<YoutubeTrackFormat> formats) {
        YoutubeTrackFormat bestFormat = null;

        for (YoutubeTrackFormat format : formats) {
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

    private static boolean isBetterFormat(YoutubeTrackFormat format, YoutubeTrackFormat other) {
        YoutubeFormatInfo info = format.getInfo();

        if (info == null) {
            return false;
        } else if (other == null) {
            return true;
        } else if (info.ordinal() != other.getInfo().ordinal()) {
            return info.ordinal() < other.getInfo().ordinal();
        } else {
            return format.getBitrate() > other.getBitrate();
        }
    }

    public boolean isSet() {
        try {
            readLock.lock();
            return cachedFormatWithUrl != null;
        } finally {
            readLock.unlock();
        }
    }

    public AudioTrackInfo process() throws Exception {
        if (!trackInfo.isStream) {
            try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
                loadBestFormatWithUrl(httpInterface);
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

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        if (trackInfo.isStream) {
            super.process(localExecutor);
        } else {
            try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
                FormatWithUrl format;
                readLock.lock();
                if (this.cachedFormatWithUrl != null) {
                    readLock.unlock();
                    format = cachedFormatWithUrl;
                } else {
                    readLock.unlock();
                    format = loadBestFormatWithUrl(httpInterface);
                }
                processStatic(localExecutor, httpInterface, format);
            }
        }
    }

    private void processStatic(LocalAudioTrackExecutor localExecutor, HttpInterface httpInterface, FormatWithUrl format) throws Exception {
        try (YoutubePersistentHttpStream stream = new YoutubePersistentHttpStream(httpInterface, format.signedUrl, format.details.getContentLength())) {
            if (format.details.getType().getMimeType().endsWith("/webm")) {
                processDelegate(new MatroskaAudioTrack(trackInfo, stream), localExecutor);
            } else {
                processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
            }
        }
    }

    private FormatWithUrl loadBestFormatWithUrl(HttpInterface httpInterface) throws Exception {
        try {
            writeLock.lock();
            if (this.cachedFormatWithUrl == null) {
                YoutubeTrackDetails details = sourceManager.getTrackDetailsLoader()
                        .loadDetails(httpInterface, getIdentifier(), true, sourceManager);

                // If the error reason is "Video unavailable" details will return null
                if (details == null) {
                    throw new FriendlyException("This video is not available", FriendlyException.Severity.COMMON, null);
                }

                List<YoutubeTrackFormat> formats = details.getFormats(httpInterface, sourceManager.getSignatureResolver());

                YoutubeTrackFormat format = findBestSupportedFormat(formats);

                URI signedUrl = sourceManager.getSignatureResolver()
                        .resolveFormatUrl(httpInterface, details.getPlayerScript(), format);
                FormatWithUrl formatWithUrl = new FormatWithUrl(format, signedUrl);

                setCachedInfo((ChuuAudioTrackInfo) details.getTrackInfo(), formatWithUrl);
            }
            return cachedFormatWithUrl;
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

    private record FormatWithUrl(YoutubeTrackFormat details,
                                 URI signedUrl) {
    }

}
