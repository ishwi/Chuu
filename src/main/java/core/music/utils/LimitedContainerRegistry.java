package core.music.utils;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.container.flac.FlacContainerProbe;
import com.sedmelluq.discord.lavaplayer.container.matroska.MatroskaContainerProbe;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3ContainerProbe;
import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegContainerProbe;
import com.sedmelluq.discord.lavaplayer.container.ogg.OggContainerProbe;
import com.sedmelluq.discord.lavaplayer.container.wav.WavContainerProbe;

import java.util.List;

public class LimitedContainerRegistry extends MediaContainerRegistry {

    public LimitedContainerRegistry() {
        super(List.of( // FLAC
                new FlacContainerProbe(),
                new MatroskaContainerProbe(), // MKV
                new MpegContainerProbe(), // MP4
                new Mp3ContainerProbe(), // MP3
                new OggContainerProbe(), // OGG
                new WavContainerProbe() // WAV
        ));

    }
}
