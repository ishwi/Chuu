package core.music.sources.youtube;

import com.sedmelluq.discord.lavaplayer.source.youtube.*;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import core.music.sources.youtube.webscrobbler.ChuuYoutubeAudioTrack;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public record LoadingRoutes(boolean allowSearch,
                            YoutubeAudioSourceManager sourceManager,
                            YoutubePlaylistLoader playlistLoader,
                            YoutubeMixLoader mixLoader,
                            YoutubeSearchResultLoader searchResultLoader,
                            YoutubeSearchMusicResultLoader searchMusicResultLoader) implements YoutubeLinkRouter.Routes<AudioItem> {
    private static final Logger log = LoggerFactory.getLogger(LoadingRoutes.class);

    private YoutubeAudioTrack buildTrackFromInfo(AudioTrackInfo info) {
        return new ChuuYoutubeAudioTrack(info, sourceManager);
    }


    @Override
    public AudioItem track(String videoId) {
        return sourceManager.loadTrackWithVideoId(videoId, false);
    }

    @Override
    public AudioItem playlist(String playlistId, String selectedVideoId) {
        log.debug("Starting to load playlist with ID {}", playlistId);

        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
            return playlistLoader.load(httpInterface, playlistId, selectedVideoId,
                    this::buildTrackFromInfo);
        } catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions(e);
        }
    }

    @Override
    public AudioItem mix(String mixId, String selectedVideoId) {
        log.debug("Starting to load mix with ID {} selected track {}", mixId, selectedVideoId);

        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
            return mixLoader.load(httpInterface, mixId, selectedVideoId,
                    this::buildTrackFromInfo);
        } catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions(e);
        }
    }

    @Override
    public AudioItem search(String query) {
        if (allowSearch) {
            return searchResultLoader.loadSearchResult(
                    query,
                    this::buildTrackFromInfo
            );
        }
        return null;

    }

    @Override
    public AudioItem searchMusic(String query) {
        if (allowSearch) {
            return searchMusicResultLoader.loadSearchMusicResult(
                    query,
                    this::buildTrackFromInfo
            );
        }
        return null;

    }

    @Override
    public AudioItem anonymous(String videoIds) {
        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
            try (CloseableHttpResponse response = httpInterface.execute(new HttpGet("https://www.youtube.com/watch_videos?video_ids=" + videoIds))) {
                HttpClientTools.assertSuccessWithContent(response, "playlist response");
                HttpClientContext context = httpInterface.getContext();
                // youtube currently transforms watch_video links into a link with a video id and a list id.
                // because thats what happens, we can simply re-process with the redirected link
                List<URI> redirects = context.getRedirectLocations();
                if (redirects != null && !redirects.isEmpty()) {
                    return new AudioReference(redirects.get(0).toString(), null);
                } else {
                    throw new FriendlyException("Unable to process youtube watch_videos link", SUSPICIOUS,
                            new IllegalStateException("Expected youtube to redirect watch_videos link to a watch?v={id}&list={list_id} link, but it did not redirect at all"));
                }
            }
        } catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions(e);
        }
    }

    @Override
    public AudioItem none() {
        return AudioReference.NO_TRACK;
    }
}

