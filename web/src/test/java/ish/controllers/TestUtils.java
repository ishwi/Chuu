package ish.controllers;

import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import io.micronaut.context.annotation.Value;
import org.junit.jupiter.api.BeforeAll;

import javax.inject.Inject;
import java.util.List;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TestUtils {
    static ChuuService chuuService;
    static LastFMData data;
    static long artistId;


    @BeforeAll
    static void init() throws InstanceNotFoundException {
        chuuService = new ChuuService();
        data = generator("test-user");
        chuuService.insertNewUser(data);
        chuuService.insertArtistDataList(listGenerator(data), data.getName());
        artistId = chuuService.getArtistId("chuubottest");

    }

    private static LastFMData generator(String lastfmid) {
        return new LastFMData(lastfmid,
                -1L,
                -1L, false, false, WhoKnowsMode.IMAGE, ChartMode.IMAGE, RemainingImagesMode.IMAGE,
                1, 1, PrivacyMode.NORMAL,
                true, false, TimeZone.getDefault());

    }

    private static List<ScrobbledArtist> listGenerator(LastFMData data) {
        return List.of(new ScrobbledArtist(data.getName(), "chuubottest", 1));
    }
}
