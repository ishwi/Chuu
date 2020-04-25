package dao.entities;

public enum Metrics {
    AOTY_DISCOGS(1), AOTY_MB_NAME(2), AOTY_TOTAL(3), LASTFM_PETITIONS(5), REQUESTED(4);

    private final int metricId;

    Metrics(int i) {
        metricId = i;
    }

    public int getMetricId() {
        return metricId;
    }
}
