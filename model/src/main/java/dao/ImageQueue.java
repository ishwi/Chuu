package dao;

import java.time.LocalDateTime;

public record ImageQueue(long queuedId, String url, long artistId, long uploader, String artistName,
                         LocalDateTime localDateTime, int userRejectedCount, int count, int strikes) {

}
