package core.music.utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import core.apis.last.entities.Scrobble;
import core.music.sources.youtube.webscrobbler.YoutubeFilters;
import core.music.sources.youtube.webscrobbler.processers.ChuuAudioTrackInfo;
import core.music.sources.youtube.webscrobbler.processers.Processed;
import dao.entities.Metadata;

import java.util.List;

public record InnerScrobble(String artist, String song, String album, String image, Long duration,
                            List<Processed> processeds) {

    InnerScrobble withDuration(Long duration) {
        return new InnerScrobble(artist, song, album, image, duration, processeds);
    }

    InnerScrobble withArtist(String artist) {
        return new InnerScrobble(artist, song, album, image, duration, processeds);
    }

    InnerScrobble withSong(String song) {
        return new InnerScrobble(artist, song, album, image, duration, processeds);
    }

    InnerScrobble withProcessed(List<Processed> processeds) {
        return new InnerScrobble(artist, song, album, image, duration, processeds);
    }

    InnerScrobble withAlbum(String album) {
        return new InnerScrobble(artist, song, album, image, duration, processeds);
    }

    InnerScrobble withImage(String image) {
        return new InnerScrobble(artist, song, album, image, duration, processeds);
    }


    InnerScrobble withMetadata(Metadata metadata) {
        if (metadata == null) {
            return this;
        }
        return this.withSong(metadata.song())
                .withAlbum(metadata.album())
                .withImage(metadata.image())
                .withArtist(metadata.artist());
    }


    InnerScrobble fromAudioTrack(AudioTrackInfo a) {
        if (a instanceof ChuuAudioTrackInfo cu) {
            return fromChuu(cu);
        }

        return withArtist(a.author).withSong(a.title).withDuration(a.length != Long.MAX_VALUE ? a.length : null);

    }

    Scrobble toScrobble() {
        return new Scrobble(artist, album, song, image, duration);
    }

    InnerScrobble withFilter() {
        return this
                .withAlbum(YoutubeFilters.doFilters(album))
                .withArtist(YoutubeFilters.doFilters(artist))
                .withSong(YoutubeFilters.doFilters(song));
    }

    InnerScrobble fromChuu(ChuuAudioTrackInfo chA) {
        var processed = chA.processed;
        Processed p = processed.get(0);
        var b = this;
        if (processed.size() > 1) {
            b = this.withDuration(processed.get(1).msStart());
        }
        return b
                .withAlbum(p.album())
                .withArtist(p.artist())
                .withSong(p.song())
                .withProcessed(processed);
    }

}
