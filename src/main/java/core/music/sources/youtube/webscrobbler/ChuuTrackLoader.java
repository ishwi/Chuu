package core.music.sources.youtube.webscrobbler;

import com.sedmelluq.discord.lavaplayer.source.youtube.DefaultYoutubeTrackDetailsLoader;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeClientConfig;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackDetails;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackDetailsLoader;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackJsonData;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import core.music.sources.youtube.webscrobbler.processers.ChapterProcesser;
import core.music.sources.youtube.webscrobbler.processers.ChuuYoutubeTrackDetails;
import core.music.sources.youtube.webscrobbler.processers.DescriptionProcesser;
import core.music.sources.youtube.webscrobbler.processers.Processed;
import core.music.sources.youtube.webscrobbler.processers.TitleProcesser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.sedmelluq.discord.lavaplayer.tools.ExceptionTools.throwWithDebugInfo;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;

public class ChuuTrackLoader extends DefaultYoutubeTrackDetailsLoader implements YoutubeTrackDetailsLoader {
    private static final Logger log = LoggerFactory.getLogger(DefaultYoutubeTrackDetailsLoader.class);


    @Override
    public YoutubeTrackDetails loadDetails(HttpInterface httpInterface, String videoId, boolean requireFormats, YoutubeAudioSourceManager sourceManager, YoutubeClientConfig clientConfig) {
        try {
            return load(httpInterface, videoId, requireFormats, sourceManager, clientConfig);
        } catch (IOException e) {
            throw ExceptionTools.toRuntimeException(e);
        }
    }

    @Override
    public YoutubeTrackDetails loadDetails(HttpInterface httpInterface, String videoId, boolean requireFormats, YoutubeAudioSourceManager sourceManager) {
        try {
            return load(httpInterface, videoId, requireFormats, sourceManager, null);
        } catch (IOException e) {
            throw ExceptionTools.toRuntimeException(e);
        }
    }


    private YoutubeTrackDetails load(
            HttpInterface httpInterface,
            String videoId,
            boolean requireFormats,
            YoutubeAudioSourceManager manager,
            YoutubeClientConfig clientConfig
    ) throws IOException {

        JsonBrowser mainInfo = loadTrackInfoFromInnertube(httpInterface, videoId, manager, null, clientConfig);
        try {
            YoutubeTrackJsonData initialData = loadBaseResponse(mainInfo, httpInterface, videoId, manager);
            if (initialData == null) {
                return null;
            }
            String responseVideoId = initialData.playerResponse.get("videoDetails").get("videoId").text();

            if (!videoId.equals(responseVideoId)) {
                if (clientConfig == null) {
                    log.warn("Received different YouTube video ({}, want: {}) to what was requested, retrying with WEB client...", responseVideoId, videoId);
                    return load(httpInterface, videoId, requireFormats, manager, YoutubeClientConfig.WEB);
                }

                throw new FriendlyException("Video returned by YouTube isn't what was requested", COMMON,
                        new IllegalStateException(initialData.playerResponse.format()));
            }



            List<Processed> process = new ChapterProcesser().process(initialData.playerResponse, mainInfo);
//            if (process == null) {
//                try {
//                    long length = getLength(mainInfo);
//                    if (length > 500) {
//                         TODO not working anymore
//                        JsonBrowser trackInfoFromMainPage = loadTrackInfoFromMainPage(httpInterface, videoId);
//                        process = new ChapterProcesser().process(initialData.playerResponse, trackInfoFromMainPage);
//                    }
//                } catch (Exception e) {
//                    Chuu.getLogger().warn("Error doing extraction from metadata");
//                }
//            }
            if (process == null) {
                process = new DescriptionProcesser().process(initialData.playerResponse, mainInfo);
            }
            if (process == null) {
                process = new TitleProcesser().process(initialData.playerResponse, mainInfo);
            }
            YoutubeTrackJsonData finalData = augmentWithPlayerScript(initialData, httpInterface, videoId, requireFormats);
            return new ChuuYoutubeTrackDetails(videoId, finalData, process);
        } catch (FriendlyException e) {
            throw e;
        } catch (Exception e) {
            throw throwWithDebugInfo(log, e, "Error when extracting data", "mainJson", mainInfo.format());
        }
    }


    private long getLength(JsonBrowser info) {
        return info.get("videoDetails").get("lengthSeconds").asLong(-1);
    }

}
