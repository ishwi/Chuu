package ish.controllers;

import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

import java.util.List;

public class TestUtils {
    static ChuuService chuuService;
    static LastFMData data;
    static long artistId;


    @BeforeAll
    static void init() throws InstanceNotFoundException {
        chuuService = new ChuuService(null);
        data = generator("test-user");
        chuuService.insertNewUser(data);
        chuuService.insertArtistDataList(listGenerator(data), data.getName());
        artistId = chuuService.getArtistId("chuubottest");

    }

    private static LastFMData generator(String lastfmid) {
        return Mockito.mock(LastFMData.class);

    }

    private static List<ScrobbledArtist> listGenerator(LastFMData data) {
        return List.of(new ScrobbledArtist(data.getName(), "chuubottest", 1));
    }
}
