package dao.entities;

import java.util.ArrayList;
import java.util.List;

public class FullAlbumEntityExtended extends FullAlbumEntity {
    private final long listeners;
    private final int totalscrobbles;
    private List<String> tagList = new ArrayList<>();
    private int totalDuration;

    public FullAlbumEntityExtended(String artist, String album, int totalPlayNumber, String albumUrl, String username, long listeners, int totalscrobbles) {
        super(artist, album, totalPlayNumber, albumUrl, username);
        this.listeners = listeners;
        this.totalscrobbles = totalscrobbles;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public long getListeners() {
        return listeners;
    }

    public int getTotalscrobbles() {
        return totalscrobbles;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }
}
