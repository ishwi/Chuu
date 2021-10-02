package ish.controllers;

import dao.ImageQueue;
import dao.entities.ImageQueueResponse;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import ish.services.ReviewService;
import ish.services.dtos.AcceptedImageDTO;
import jakarta.inject.Inject;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/image-queue")
public class ImageQueueController {
    @Inject
    private ReviewService reviewService;

    @Get(value = "{?limit,until}", produces = MediaType.APPLICATION_JSON)
    public List<ImageQueue> fetchNewImages(@QueryValue(defaultValue = "10") @Min(1) int limit, @QueryValue @Nullable Instant until) {
        return reviewService.getQueuedImages(limit, until == null ? Instant.now().plus(10, ChronoUnit.DAYS) : until);

    }

    @Post("/accept")
    @Status(HttpStatus.OK)
    public void acceptImage(@Body @Valid AcceptedImageDTO acceptedImageDTO) {
        reviewService.acceptImage(acceptedImageDTO);

    }

    @Post("/reject/{queuedId}")
    @Status(HttpStatus.OK)
    public void rejectImage(@PathVariable long queuedId) {
        reviewService.rejectImage(queuedId);
    }

    @Post("/batch")
    @Status(HttpStatus.OK)
    public void batchReview(@Body List<ImageQueueResponse> responses) {
        reviewService.batch(responses);
    }

}
