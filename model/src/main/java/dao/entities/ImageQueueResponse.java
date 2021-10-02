package dao.entities;

import dao.ImageQueue;

public record ImageQueueResponse(String url, long artistId, long ownerId, long queuedId, ResponseType responseType) {

    public ImageQueue toImage() {
        return new ImageQueue(queuedId, url, artistId, ownerId, null, null, 0, 0, 0, null);
    }

    public enum ResponseType {
        ACCEPTED, REJECTED
    }

}
