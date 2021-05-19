package dao.entities;

import java.util.List;

public class FullAlbumEntityExtended extends FullAlbumEntity {
    private final long listeners;
    private final int totalscrobbles;
    private final String biography;
    private final List<String> tagList;
    private int totalDuration;


    public FullAlbumEntityExtended(String artist, String album, int totalPlayNumber, String albumUrl, String username, long listeners, int totalscrobbles, List<String> tagList, String biography) {
        super(artist, album, totalPlayNumber, albumUrl, username);
        this.listeners = listeners;
        this.totalscrobbles = totalscrobbles;
        this.tagList = tagList;
        this.biography = biography;
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

    public String getBiography() {
        return biography;
    }
}
