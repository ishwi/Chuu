package core.music.sources.youtube.webscrobbler;

import com.sedmelluq.discord.lavaplayer.source.youtube.*;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import core.music.sources.youtube.webscrobbler.processers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.sedmelluq.discord.lavaplayer.tools.ExceptionTools.throwWithDebugInfo;

public class ChuuTrackLoader extends DefaultYoutubeTrackDetailsLoader implements YoutubeTrackDetailsLoader {
    private static final Logger log = LoggerFactory.getLogger(DefaultYoutubeTrackDetailsLoader.class);


    @Override
    public YoutubeTrackDetails loadDetails(HttpInterface httpInterface, String videoId, boolean requireFormats, YoutubeAudioSourceManager manager) {
        try {
            return this.load(httpInterface, videoId, requireFormats, manager);
        } catch (IOException e) {
            throw ExceptionTools.toRuntimeException(e);
        }
    }

    private YoutubeTrackDetails load(
            HttpInterface httpInterface,
            String videoId,
            boolean requireFormats,
            YoutubeAudioSourceManager manager
    ) throws IOException {
        JsonBrowser mainInfo = loadTrackInfoFromInnertube(httpInterface, videoId, manager, null);
        try {
            YoutubeTrackJsonData initialData = loadBaseResponse(mainInfo, httpInterface, videoId, manager);
            if (initialData == null) {
                return null;
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
