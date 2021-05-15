package core.music.sources.youtube.webscrobbler;

import com.sedmelluq.discord.lavaplayer.source.youtube.DefaultYoutubeTrackDetailsLoader;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackDetails;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackDetailsLoader;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeTrackJsonData;
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

    private static final String[] EMBED_CONFIG_PREFIXES = new String[]{
            "'WEB_PLAYER_CONTEXT_CONFIGS':",
            "WEB_PLAYER_CONTEXT_CONFIGS\":",
            "'PLAYER_CONFIG':",
            "\"PLAYER_CONFIG\":"
    };


    @Override
    public YoutubeTrackDetails loadDetails(HttpInterface httpInterface, String videoId, boolean requireFormats) {
        try {
            return this.load(httpInterface, videoId, requireFormats);
        } catch (IOException e) {
            throw ExceptionTools.toRuntimeException(e);
        }
    }

    private YoutubeTrackDetails load(
            HttpInterface httpInterface,
            String videoId,
            boolean requireFormats
    ) throws IOException {
        JsonBrowser mainInfo = loadTrackInfoFromMainPage(httpInterface, videoId);
        try {
            YoutubeTrackJsonData initialData = loadBaseResponse(mainInfo, httpInterface, videoId, requireFormats);
            if (initialData == null) {
                return null;
            }
            List<Processed> process = new ChapterProcesser().process(initialData.playerResponse, mainInfo);
            if (process == null) {
                process = new DescriptionProcesser().process(initialData.playerResponse, mainInfo);
            }
            if (process == null) {
                process = new TitleProcesser().process(initialData.playerResponse, mainInfo);
            }
            YoutubeTrackJsonData finalData = augmentWithPlayerScript(initialData, httpInterface, requireFormats);
            return new ChuuYoutubeTrackDetails(videoId, finalData, process);
        } catch (FriendlyException e) {
            throw e;
        } catch (Exception e) {
            throw throwWithDebugInfo(log, e, "Error when extracting data", "mainJson", mainInfo.format());
        }
    }


}
