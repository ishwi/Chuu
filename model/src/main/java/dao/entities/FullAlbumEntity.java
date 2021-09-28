package dao.entities;

import java.util.ArrayList;
import java.util.List;

public class FullAlbumEntity {
    private final String artist;
    private final String album;
    private final int totalPlayNumber;
    private final String username;
    private String albumUrl;
    private List<Track> trackList = new ArrayList<>();
    private String artistUrl;
    private String mbid;

    public FullAlbumEntity(String artist, String album, int totalPlayNumber, String albumUrl, String username) {
        this.artist = artist;
        this.album = album;
        this.totalPlayNumber = totalPlayNumber;
        this.albumUrl = albumUrl;
        this.username = username;
    }

    public FullAlbumEntity(FullAlbumEntity other) {
        this.artist = other.artist;
        this.album = other.album;
        this.totalPlayNumber = other.totalPlayNumber;
        this.albumUrl = other.albumUrl;
        this.username = other.username;
        this.trackList = other.trackList;
        this.artistUrl = other.artistUrl;
        this.mbid = other.mbid;
    }

    public String getUsername() {
        return username;
    }

    public String getArtistUrl() {
        return artistUrl;
    }

    public void setArtistUrl(String artistUrl) {
        this.artistUrl = artistUrl;
    }

    public String getAlbumUrl() {
        return albumUrl == null || albumUrl.isBlank() ? null : albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public void addTrack(Track track) {
        trackList.add(track);
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public int getTotalPlayNumber() {
        if (totalPlayNumber == 0) {
            return trackList.stream().mapToInt(Track::getPlays).sum();
        }
        return totalPlayNumber;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }
}
