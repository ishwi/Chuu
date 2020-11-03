package ish.services;

import dao.ChuuService;
import dao.ImageQueue;
import dao.entities.ImageQueueResponse;
import dao.entities.ReportEntity;
import ish.services.dtos.AcceptedImageDTO;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Singleton
public class ReviewService {
    @Inject
    private ChuuService chuuService;

    public ReportEntity getReport() {
        return chuuService.getNextReport(LocalDateTime.now(), new HashSet<>());
    }

    public List<ImageQueue> getQueuedImages(int limit, Instant untill) {
        return chuuService.getAllImageQueue(untill, limit);
    }

    public void rejectImage(long queuedId) {
        chuuService.rejectQueuedImage(queuedId);
    }

    public void acceptImage(AcceptedImageDTO acceptedImageDTO) {
        chuuService.acceptImageQueue(acceptedImageDTO.queuedId(), acceptedImageDTO.url(), acceptedImageDTO.artistId(), acceptedImageDTO.uploader());
    }

    public void batch(List<ImageQueueResponse> responses) {
        if (responses.stream().anyMatch(x -> x.getResponseType() == ImageQueueResponse.ResponseType.ACCEPTED && (x.getOnwerId() == null || x.getArtistId() == null || x.getUrl() == null))) {
            throw new IllegalStateException();
        }
        chuuService.batchAcceptReviews(responses);
    }
}
