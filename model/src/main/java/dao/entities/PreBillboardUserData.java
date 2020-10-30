package dao.entities;

public class PreBillboardUserData {
    private final long artistId;
    private final String lastfmId;
    private final String trackName;
    private final int playCount;

    public PreBillboardUserData(long artistId, String lastfmId, String trackName, int playCount) {
        this.artistId = artistId;
        this.lastfmId = lastfmId;
        this.trackName = trackName;
        this.playCount = playCount;
    }

    public long getArtistId() {
        return artistId;
    }

    public String getLastfmId() {
        return lastfmId;
    }

    public String getTrackName() {
        return trackName;
    }

    public int getPlayCount() {
        return playCount;
    }
}
