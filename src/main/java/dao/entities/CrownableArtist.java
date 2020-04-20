package dao.entities;

public class CrownableArtist {
    private final String artistName;
    private final int playNumber;
    private final int maxPlaynumber;
    private final int rank;
    private final int totalListeners;


    public CrownableArtist(String artistName, int playNumber, int maxPlaynumber, int rank, int totalListeners) {
        this.artistName = artistName;
        this.playNumber = playNumber;
        this.maxPlaynumber = maxPlaynumber;
        this.rank = rank;
        this.totalListeners = totalListeners;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getPlayNumber() {
        return playNumber;
    }

    public int getMaxPlaynumber() {
        return maxPlaynumber;
    }

    public int getRank() {
        return rank;
    }

    public int getTotalListeners() {
        return totalListeners;
    }
}
