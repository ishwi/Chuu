package core.apis.last;

import dao.entities.Track;

import java.util.List;

public class TrackExtended extends Track {

    private final List<String> tags;
    private final int totalPlayCount;
    private final int listeners;
    private final String albumName;


    public TrackExtended(String re_artist, String re_trackName, int userplaycount, boolean userloved, int duration, List<String> tags, int totalPlayCount, int listeners, String albumName) {
        super(re_artist, re_trackName, userplaycount, userloved, duration);
        this.tags = tags;
        this.totalPlayCount = totalPlayCount;
        this.listeners = listeners;
        this.albumName = albumName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public List<String> getTags() {
        return tags;
    }

    public int getTotalPlayCount() {
        return totalPlayCount;
    }

    public int getListeners() {
        return listeners;
    }
}
