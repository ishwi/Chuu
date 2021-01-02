package core.music.radio;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class ScrobblerAudioPlayer extends DefaultAudioPlayer {
    /**
     * @param manager Audio player manager which this player is attached to
     */
    public ScrobblerAudioPlayer(DefaultAudioPlayerManager manager) {
        super(manager);
    }

    @Override
    public boolean startTrack(AudioTrack track, boolean noInterrupt) {
        return super.startTrack(track, noInterrupt);
    }
}
