package dao.entities;

import java.time.Instant;

public class StreakEntity {
    public static final short MAX_STREAK = 6001;
    private final String currentArtist;
    private final int aCounter;
    private final String currentAlbum;
    private final int albCounter;
    private final String currentSong;
    private final int tCounter;
    private Instant streakStart;

    public StreakEntity(String currentArtist, int aCounter, String currentAlbum, int albCounter, String currentSong, int tCounter, Instant streakStart) {
        this.currentArtist = currentArtist;
        this.aCounter = aCounter;
        this.currentAlbum = currentAlbum;
        this.albCounter = albCounter;
        this.currentSong = currentSong;
        this.tCounter = tCounter;
        this.streakStart = streakStart;
    }

    public String getCurrentArtist() {
        return currentArtist;
    }

    public int getaCounter() {
        return aCounter;
    }

    public String getCurrentAlbum() {
        return currentAlbum;
    }

    public int getAlbCounter() {
        return albCounter;
    }

    public String getCurrentSong() {
        return currentSong;
    }

    public int gettCounter() {
        return tCounter;
    }

    public Instant getStreakStart() {
        return streakStart;
    }

    public void setStreakStart(Instant streakStart) {
        this.streakStart = streakStart;
    }
}
