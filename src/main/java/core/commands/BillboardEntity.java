package core.commands;

public class BillboardEntity {
    private final String artist;
    private final String name;
    private final Long listeners;
    private final int peak;
    private final int streak;
    private final int previousWeek;
    private final int position;
    private long artistId;
    private final String url;

    public BillboardEntity(String artist, String name, Long listeners, int peak, int streak, int previousWeek, int position, String url) {
        this.artist = artist;
        this.name = name;
        this.listeners = listeners;
        this.peak = peak;
        this.streak = streak;
        this.previousWeek = previousWeek;
        this.position = position;
        this.url = url;
    }

    public BillboardEntity(String artist, String name, Long listeners) {
        this.artist = artist;
        this.name = name;
        this.listeners = listeners;
        peak = -1;
        streak = -1;
        previousWeek = -1;
        position = -1;
        url = null;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public Long getListeners() {

        return listeners;

    }

    public int getPeak() {
        return peak;
    }

    public int getStreak() {
        return streak;
    }

    public int getPreviousWeek() {
        return previousWeek;
    }

    public int getPosition() {
        return position;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getUrl() {
        return url;
    }
}
