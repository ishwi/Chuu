package core.music.utils;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import core.music.sources.youtube.ChuuYTAudioSourceManager;

public class YoutubeSearchManagerSingleton {
    private static YoutubeAudioSourceManager instance;

    private YoutubeSearchManagerSingleton() {
    }


    public static synchronized YoutubeAudioSourceManager getInstance() {
        if (instance == null) {
            instance = new ChuuYTAudioSourceManager(true);
        }
        return instance;
    }

}
