package core.music.sources.youtube.webscrobbler.processers;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;

import java.util.List;

public interface YtProcessser {

    List<Processed> process(JsonBrowser details, JsonBrowser main);
}
