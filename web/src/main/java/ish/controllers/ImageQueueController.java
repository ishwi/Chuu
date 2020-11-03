package ish.controllers;

import dao.ImageQueue;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import ish.services.ReviewService;
import ish.services.dtos.AcceptedImageDTO;
import dao.entities.ImageQueueResponse;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;


@PermitAll
@Controller("/image-queue")
public class ImageQueueController {
    @Inject
    private ReviewService reviewService;

    @Get(value = "{?limit,until}", produces = MediaType.APPLICATION_JSON)
    public List<ImageQueue> index(@QueryValue(defaultValue = "10") @Min(1) int limit, @QueryValue @Nullable Instant until) {
        return reviewService.getQueuedImages(limit, until == null ? Instant.now().plus(10, ChronoUnit.DAYS) : until);

    }

    @Post("/accept")
    @Status(HttpStatus.OK)
    public void accept(@Body @Valid AcceptedImageDTO acceptedImageDTO) {
        reviewService.acceptImage(acceptedImageDTO);

    }

    @Post("/reject/{queuedId}")
    @Status(HttpStatus.OK)
    public void reject(@Body long queuedId) {
        reviewService.rejectImage(queuedId);
    }

    @Post("/batch")
    @Status(HttpStatus.OK)
    public void batch(@Body List<ImageQueueResponse> responses) {
        reviewService.batch(responses);
    }

}
