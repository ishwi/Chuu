package core.music.sources.youtube;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.*;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import core.music.sources.youtube.webscrobbler.ChuuTrackLoader;
import core.music.sources.youtube.webscrobbler.processers.ChuuAudioTrackInfo;
import core.music.sources.youtube.webscrobbler.processers.ChuuYoutubeTrackDetails;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.FAULT;

public class ChuuYTAudioSourceManager extends YoutubeAudioSourceManager {
    private final LoadingRoutes loadingRoutes;
    private final YoutubeLinkRouter linkRouter;

    public ChuuYTAudioSourceManager(boolean allowSearch, String email, String password) {
        this(allowSearch, email, password, new ChuuTrackLoader(), new YoutubeSearchProvider(),
                new YoutubeSearchMusicProvider(),
                new YoutubeSignatureCipherManager(),
                new DefaultYoutubePlaylistLoader(),
                new DefaultYoutubeLinkRouter(),
                new YoutubeMixProvider());
    }


    public ChuuYTAudioSourceManager(boolean allowSearch,
                                    String email,
                                    String password,
                                    YoutubeTrackDetailsLoader trackDetailsLoader,
                                    YoutubeSearchResultLoader searchResultLoader,
                                    YoutubeSearchMusicResultLoader searchMusicResultLoader,
                                    YoutubeSignatureResolver signatureResolver,
                                    YoutubePlaylistLoader playlistLoader,
                                    YoutubeLinkRouter linkRouter,
                                    YoutubeMixLoader mixLoader) {
        super(allowSearch, email, password, trackDetailsLoader, searchResultLoader, searchMusicResultLoader, signatureResolver, playlistLoader, linkRouter, mixLoader);
        this.loadingRoutes = new LoadingRoutes(allowSearch, this, playlistLoader, mixLoader, searchResultLoader, searchMusicResultLoader);
        this.linkRouter = linkRouter;
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        try {
            return loadItemOnce(reference);
        } catch (FriendlyException exception) {
            // In case of a connection reset exception, try once more.
            if (HttpClientTools.isRetriableNetworkException(exception.getCause())) {
                return loadItemOnce(reference);
            } else {
                throw exception;
            }
        }
    }

    private AudioItem loadItemOnce(AudioReference reference) {
        return linkRouter.route(reference.identifier, loadingRoutes);
    }

    public AudioItem loadTrackWithVideoId(String videoId, boolean mustExist) {
        try (HttpInterface httpInterface = getHttpInterface()) {
            YoutubeTrackDetails details = this.getTrackDetailsLoader().loadDetails(httpInterface, videoId, false, this);

            if (details == null) {
                if (mustExist) {
                    throw new FriendlyException("Video unavailable", COMMON, null);
                } else {
                    return AudioReference.NO_TRACK;
                }
            }
            return new YoutubeAudioTrack(new ChuuAudioTrackInfo(details.getTrackInfo(), (ChuuYoutubeTrackDetails) details), this);
        } catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Loading information for a YouTube track failed.", FAULT, e);
        }
    }


}
