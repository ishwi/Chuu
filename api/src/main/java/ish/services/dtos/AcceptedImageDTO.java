package ish.services.dtos;


import io.micronaut.core.annotation.Introspected;

@Introspected
public record AcceptedImageDTO(long queuedId, long uploader, String url, long artistId) {


}
