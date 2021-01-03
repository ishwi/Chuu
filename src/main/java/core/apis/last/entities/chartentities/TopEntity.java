package core.apis.last.entities.chartentities;


import core.apis.last.ConcurrentLastFM;

public enum TopEntity {
    ALBUM, TRACK, ARTIST;


    public String getLeadingObject() {
        return switch (this) {
            case ALBUM -> "topalbums";
            case TRACK -> "toptracks";
            case ARTIST -> "topartists";
        };
    }

    public String getApiMethod() {
        return switch (this) {
            case ALBUM -> ConcurrentLastFM.GET_ALBUMS;
            case TRACK -> ConcurrentLastFM.GET_TOP_TRACKS;
            case ARTIST -> ConcurrentLastFM.GET_ARTIST;
        };
    }

    public String getArrayObject() {
        return switch (this) {
            case ALBUM -> "album";
            case TRACK -> "track";
            case ARTIST -> "artist";
        };
    }


    public String getCustomLeadingObject() {
        return switch (this) {
            case ALBUM -> "weeklyalbumchart";
            case TRACK -> "weeklytrackchart";
            case ARTIST -> "weeklyartistchart";
        };
    }

    public String getCustomApiMethod() {
        return switch (this) {
            case ALBUM -> ConcurrentLastFM.GET_WEEKLY_CHART_ALBUM;
            case TRACK -> ConcurrentLastFM.GET_WEEKLY_CHART_TRACK;
            case ARTIST -> ConcurrentLastFM.GET_WEEKLY_CHART_ARTIST;
        };
    }

    public String getCustomArrayObject() {
        return getArrayObject();
    }


}
