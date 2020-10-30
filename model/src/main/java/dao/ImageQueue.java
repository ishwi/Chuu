package dao;

import java.time.LocalDateTime;

public class ImageQueue {
    private final long queuedId;
    private final String url;
    private final long artistId;
    private final long uploader;
    private final String artistName;
    private final LocalDateTime localDateTime;
    private final int userReportCount;

    public ImageQueue(long queuedId, String url, long artistId, long uploader, String artistName, LocalDateTime localDateTime, int userReportCount) {
        super();
        this.queuedId = queuedId;
        this.url = url;
        this.artistId = artistId;
        this.uploader = uploader;
        this.artistName = artistName;
        this.localDateTime = localDateTime;
        this.userReportCount = userReportCount;
    }

    public long getQueuedId() {
        return queuedId;
    }

    public String getUrl() {
        return url;
    }

    public long getArtistId() {
        return artistId;
    }

    public long getUploader() {
        return uploader;
    }

    public String getArtistName() {
        return artistName;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public int getUserReportCount() {
        return userReportCount;
    }
}
