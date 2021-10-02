package ish.controllers;

import dao.ChuuService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
@TestMethodOrder(MethodOrderer.Random.class)
public class ImageQueueTest extends TestUtils {
    @Inject
    private ChuuService chuuService;


    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/image-queue")
    HttpClient client;

    @SuppressWarnings("rawtypes")
    @Test
    void testHelloWorldResponse() {
        List<HashMap> response = client.toBlocking().retrieve(HttpRequest.GET("?limit=" + Integer.MAX_VALUE), Argument.listOf(Argument.of(HashMap.class)));
        chuuService.userInsertQueueUrl(UUID.randomUUID().toString(), artistId, -1L);
        List<HashMap> response2 = client.toBlocking().retrieve(HttpRequest.GET("?limit=" + Integer.MAX_VALUE), Argument.listOf(Argument.of(HashMap.class)));
        assertEquals(response.size() + 1, response2.size());
    }

    @Test
    void testAccept() {
//        chuuService.userInsertQueueUrl(UUID.randomUUID().toString(), artistId, -1L);
//
//        ImageQueue nextQueue = chuuService.getNextQueue(null, new HashSet<>());
//        assertEquals(client.toBlocking().exchange(HttpRequest.POST("accept", nextQueue)).code(), HttpStatus.OK.getCode());
//        ImageQueue nextQueue2 = chuuService.getNextQueue(LocalDateTime.now().plus(10, ChronoUnit.DAYS), new HashSet<>());
//        assertNotEquals(nextQueue, nextQueue2);
//        Optional<VotingEntity> first = chuuService.getAllArtistImages(nextQueue.getArtistId()).stream().filter(x -> x.getUrl().equalsIgnoreCase(nextQueue.getUrl())).findFirst();
//        assertTrue(first.isPresent());
//        VotingEntity votingEntity = first.get();
//        assertTrue(votingEntity.getTotalVotes() >= 1);
//        assertTrue(votingEntity.getVotes() >= 1);


    }

    @SuppressWarnings("rawtypes")
    @Test
    void testReject() {
//        chuuService.userInsertQueueUrl(UUID.randomUUID().toString(), artistId, -1L);
//        ImageQueue nextQueue = chuuService.getNextQueue(LocalDateTime.now().plus(10, ChronoUnit.DAYS), new HashSet<>());
//        assertEquals(client.toBlocking().exchange(HttpRequest.POST("reject/" + nextQueue.getQueuedId(), MediaType.ALL)).code(), HttpStatus.OK.getCode());
//        ImageQueue nextQueue2 = chuuService.getNextQueue(LocalDateTime.now().plus(10, ChronoUnit.DAYS), new HashSet<>());
//        assertNotEquals(nextQueue, nextQueue2);
//        Optional<VotingEntity> first = chuuService.getAllArtistImages(nextQueue.getArtistId()).stream().filter(x -> x.getUrl().equalsIgnoreCase(nextQueue.getUrl())).findFirst();
//        assertTrue(first.isEmpty());
//        List<HashMap> response = client.toBlocking().retrieve(HttpRequest.GET("?limit=" + Integer.MAX_VALUE), Argument.listOf(Argument.of(HashMap.class)));
//        assertTrue(response.stream().noneMatch(x -> (Integer) x.get("queuedId") == nextQueue.getQueuedId()));

    }

    @SuppressWarnings("rawtypes")
    @Test
    void testBatch() {
//        chuuService.userInsertQueueUrl(UUID.randomUUID().toString(), artistId, -1L);
//
//        List<HashMap> response = client.toBlocking().retrieve(HttpRequest.GET("?limit=" + 10), Argument.listOf(Argument.of(HashMap.class)));
//        Random random = new Random();
//        List<ImageQueueResponse> imageQueueResponses = new ArrayList<>();
//        int counter = 0;
//        for (HashMap hashMap : response) {
//            Integer queuedId = (Integer) hashMap.get("queuedId");
//
//            if (random.nextBoolean()) {
//                imageQueueResponses.add(new ImageQueueResponse(queuedId, ImageQueueResponse.ResponseType.REJECTED));
//            } else {
//                String url = (String) hashMap.get("url");
//                long artistId = ((Number) hashMap.get("artistId")).intValue();
//                long ownerId = ((Number) hashMap.get("uploader")).intValue();
//                counter++;
//                imageQueueResponses.add(new ImageQueueResponse(queuedId, ImageQueueResponse.ResponseType.ACCEPTED, url, (long) artistId, (long) ownerId));
//            }
//        }
//        assertEquals(client.toBlocking().exchange(HttpRequest.POST("batch/", imageQueueResponses)).code(), HttpStatus.OK.getCode());
//        List<HashMap> response2 = client.toBlocking().retrieve(HttpRequest.GET("?limit=" + 10), Argument.listOf(Argument.of(HashMap.class)));
//        Set<Integer> ids1 = response.stream().mapToInt(x -> ((Number) x.get("queuedId")).intValue()).boxed().collect(Collectors.toSet());
//        Set<Integer> ids2 = response2.stream().mapToInt(x -> ((Number) x.get("queuedId")).intValue()).boxed().collect(Collectors.toSet());
//
//        int size = ids1.size();
//        ids1.removeAll(ids2);
//        assertEquals(ids1.size(), size);
    }
}
