package ish.services;

import dao.ChuuService;
import dao.ImageQueue;
import dao.entities.ImageQueueResponse;
import dao.entities.ReportEntity;
import ish.services.dtos.AcceptedImageDTO;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Singleton
public class ReviewService {
    @Inject
    private ChuuService chuuService;

    public ReportEntity getReport() {
        return chuuService.getNextReport(null, Collections.emptySet());
    }

    public List<ImageQueue> getQueuedImages(int limit, Instant untill) {
        return chuuService.getNextQueue();
    }

    public void rejectImage(long queuedId) {
        chuuService.rejectQueuedImage(queuedId, null);
    }

    public void acceptImage(AcceptedImageDTO acceptedImageDTO) {
        chuuService.acceptImageQueue(acceptedImageDTO.queuedId(), acceptedImageDTO.url(), acceptedImageDTO.artistId(), acceptedImageDTO.uploader());
    }

    public void batch(List<ImageQueueResponse> responses) {

        responses.forEach(response -> {
            switch (response.responseType()) {
                case ACCEPTED -> chuuService.acceptImageQueue(response.queuedId(), response.url(), response.artistId(), response.ownerId());
                case REJECTED -> chuuService.rejectQueuedImage(response.queuedId(), response.toImage());
            }
        });
    }
}
