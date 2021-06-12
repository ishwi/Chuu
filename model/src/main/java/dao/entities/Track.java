package dao.entities;

import dao.utils.LinkUtils;

import java.util.Objects;

public class Track {
    private final String artist;
    private final int plays;
    private final boolean isLoved;
    private final int duration;
    private String name;
    private int position;
    private String imageUrl;
    private String mbid;

    public Track(String artist, String name, int plays, boolean isLoved, int duration) {
        this.artist = artist;
        this.name = name;
        this.plays = plays;
        this.isLoved = isLoved;
        this.duration = duration;

    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlays() {
        return plays;
    }

    public boolean isLoved() {
        return isLoved;
    }

    public int getDuration() {
        return duration;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(artist, track.artist) &&
               name.equals(track.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artist, name);
    }

    @Override
    public String toString() {

        return ". " +
               "[" +
               LinkUtils.cleanMarkdownCharacter(artist) + " - " + LinkUtils.cleanMarkdownCharacter(name) +
               "](" + LinkUtils.getLastFMArtistTrack(artist, name) +
               ")" +
               " - " + plays + " plays" +
               "\n";
    }

    public String getPie() {
        return name + " (" + plays + ")";
    }
}
