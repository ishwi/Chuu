package dao.entities;

import javax.annotation.Nullable;

public class ImageQueueResponse {
    private @Nullable
    String url;

    private @Nullable
    Long artistId;
    private @Nullable
    Long onwerId;
    private long queuedId;
    private ResponseType responseType;

    public ImageQueueResponse() {
    }

    public ImageQueueResponse(long queuedId, ResponseType responseType) {
        this.queuedId = queuedId;
        this.responseType = responseType;
    }

    public ImageQueueResponse(long queuedId, ResponseType responseType, @Nullable String url, @Nullable Long artistId, @Nullable Long onwerId) {
        this.url = url;
        this.artistId = artistId;
        this.onwerId = onwerId;
        this.queuedId = queuedId;
        this.responseType = responseType;
    }

    public long getQueuedId() {
        return queuedId;
    }

    public void setQueuedId(long queuedId) {
        this.queuedId = queuedId;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(@Nullable String url) {
        this.url = url;
    }

    @Nullable
    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(@Nullable Long artistId) {
        this.artistId = artistId;
    }

    @Nullable
    public Long getOnwerId() {
        return onwerId;
    }

    public void setOnwerId(@Nullable Long onwerId) {
        this.onwerId = onwerId;
    }

    public enum ResponseType {
        ACCEPTED, REJECTED
    }
}
