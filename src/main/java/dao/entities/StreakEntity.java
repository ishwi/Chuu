package dao.entities;

public class StreakEntity {
    private final String currentArtist;
    private final int aCounter;
    private final String currentAlbum;
    private final int albCounter;
    private final String currentSong;
    private final int tCounter;

    public StreakEntity(String currentArtist, int aCounter, String currentAlbum, int albCounter, String currentSong, int tCounter) {
        this.currentArtist = currentArtist;
        this.aCounter = aCounter;
        this.currentAlbum = currentAlbum;
        this.albCounter = albCounter;
        this.currentSong = currentSong;
        this.tCounter = tCounter;
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
}
