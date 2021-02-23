package core.music.utils;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

public class YoutubeSearchManagerSingleton {
    private static YoutubeAudioSourceManager instance;

    private YoutubeSearchManagerSingleton() {
    }


    public static synchronized YoutubeAudioSourceManager getInstance() {
        if (instance == null) {
            instance = new YoutubeAudioSourceManager(true);
        }
        return instance;
    }

}
