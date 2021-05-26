package dao.entities;

import java.util.Objects;

public class ScrobbledTrack extends ScrobbledArtist {
    private final boolean isLoved;
    private String name;
    private String mbid;
    private int duration;
    private int position;
    private String imageUrl;
    private long artistId = -1L;
    private String lastfmId;
    private String artistMbid;
    private String album;
    private String albumMbid;
    private long albumId = -1L;
    private long trackId = -1L;
    private String albumUrl;
    private String spotifyId;
    private int popularity;


    public ScrobbledTrack(String artist, String name, int plays, boolean isLoved, int duration, String imageUrl, String artistMbid, String mbid) {
        super(artist, plays, null);
        this.name = name;
        this.isLoved = isLoved;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.artistMbid = artistMbid;
        this.mbid = mbid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }


    public boolean isLoved() {
        return isLoved;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getLastfmId() {
        return lastfmId;
    }

    public void setLastfmId(String lastfmId) {
        this.lastfmId = lastfmId;
    }

    public String getArtistMbid() {
        return artistMbid;
    }

    public void setArtistMbid(String artistMbid) {
        this.artistMbid = artistMbid;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumMbid() {
        return albumMbid;
    }

    public void setAlbumMbid(String albumMbid) {
        this.albumMbid = albumMbid;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(name, ((ScrobbledTrack) o).name);
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    @Override
    public int hashCode() {
        int result = getDiscordID() != null ? getDiscordID().hashCode() : 0;
        result = 31 * result + (getArtist() != null ? getArtist().hashCode() : 0);
        result = 31 * result + getCount();
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }
}
