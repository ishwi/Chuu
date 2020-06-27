package dao.entities;

public class TrackWithArtistId extends Track {
    private long artistId;
    private int plays;

    public TrackWithArtistId(String artist, String name, int plays, boolean isLoved, int duration) {
        super(artist, name, plays, isLoved, duration);
    }


    public TrackWithArtistId(Track other) {
        this(other.getArtist(), other.getName(), other.getPlays(), other.isLoved(), other.getDuration());
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    @Override
    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }
}
