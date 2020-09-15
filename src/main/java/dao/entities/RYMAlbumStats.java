package dao.entities;

public class RYMAlbumStats {
    private final double average;
    private final int ratingCount;

    public RYMAlbumStats(double average, int ratingCount) {
        this.average = average;
        this.ratingCount = ratingCount;
    }

    public double getAverage() {
        return average;
    }

    public int getRatingCount() {
        return ratingCount;
    }
}
