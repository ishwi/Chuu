package core.music.sources.youtube.webscrobbler.processers;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;

import java.util.Collections;
import java.util.List;

public interface YtSingleProcessser extends YtProcessser {


    @Override
    default List<Processed> process(JsonBrowser details, JsonBrowser main) {
        Processed processed = processSingle(details, main);
        return processed == null ? null : Collections.singletonList(processed);
    }

    Processed processSingle(JsonBrowser details, JsonBrowser main);
}
