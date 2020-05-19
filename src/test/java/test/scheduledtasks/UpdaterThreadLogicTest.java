package test.scheduledtasks;

import core.apis.discogs.DiscogsApi;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.spotify.Spotify;
import core.commands.CommandUtil;
import core.exceptions.DiscogsServiceException;
import dao.ChuuService;
import dao.SimpleDataSource;
import dao.entities.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UpdaterThreadLogicTest {
    private Spotify spotify;
    private ChuuService dao;
    private DiscogsApi discogsApi;
    private ConcurrentLastFM lastFM;
    private SimpleDataSource dataSource;

    @After
    public void tearDown() throws Exception {
        dao.removeUserCompletely(1L);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("TRUNCATE lastfm_test.corrections");
            preparedStatement.execute();
            preparedStatement = connection
                    .prepareStatement("DELETE FROM lastfm_test.artist_url WHERE artist_id LIKE 'Raphael%' OR artist_id = 'manolo'");
            preparedStatement.execute();
        }

    }

    @Before
    public void setUp() throws SQLException, DiscogsServiceException {

        dataSource = new SimpleDataSource("/datasource.properties");
        discogsApi = mock(DiscogsApi.class);
        when(discogsApi.findArtistImage(anyString())).thenReturn("a");
        when(discogsApi.getYearRelease(anyString(), anyString())).thenReturn(Year.now());
        spotify = mock(Spotify.class);
        when(spotify.getArtistUrlImage(anyString())).thenReturn("a");

        lastFM = LastFMFactory.getNewInstance();

        dao = new ChuuService(dataSource);
        dao.insertNewUser(new LastFMData("manuelk", 1, Role.USER, privateUpdate));
        dao.insertArtistDataList(Collections.emptyList(), "manuelk");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("TRUNCATE lastfm_test.corrections");
            preparedStatement.execute();
            preparedStatement = connection
                    .prepareStatement("DELETE FROM lastfm_test.artist_url WHERE artist_id LIKE 'Raphael%' OR artist_id = 'manolo'");
            preparedStatement.execute();
        }

    }

    @Test
    public void run() throws DiscogsServiceException {
        Map<ScrobbledArtist, String> correctionAdder = new HashMap<>();

        List<ScrobbledArtist> a = new ArrayList<>();
        a.add(new ScrobbledArtist("Raphael", 1, null));
        a.add(new ScrobbledArtist("Raphael1", 2, null));
        a.add(new ScrobbledArtist("Raphael2", 3, null));
        a.add(new ScrobbledArtist("Raphael3", 4, null));

        TimestampWrapper<List<ScrobbledArtist>> artistDataLinkedList = new TimestampWrapper<>(a, Instant.now()
                                                                                                         .get(ChronoField.MILLI_OF_SECOND) * 1000);
        //Correction with current last fm implementation should return the same name so no correction gives
        for (ScrobbledArtist datum : artistDataLinkedList.getWrapped()) {
            CommandUtil.valiate(dao, datum, lastFM, discogsApi, spotify, correctionAdder);
        }

        dao.incrementalUpdate(artistDataLinkedList, "manuelk");
        correctionAdder.forEach((artistData, s) -> dao.insertCorrection(s, artistData.getArtist()));

        dao.upsertUrl(new ArtistInfo("b", "manolo"));

        lastFM = mock(ConcurrentLastFM.class);

        when(lastFM.getCorrection(anyString())).thenAnswer(invocation ->
                "axdasd" + invocation.getArgument(0, String.class) + "ax" + LocalDateTime.now().getDayOfYear()
        );

        a = new ArrayList<>();
        new ScrobbledArtist("Raphael1", 2, null);
        a.add(new ScrobbledArtist("Raphael4", 1, null));
        a.add(new ScrobbledArtist("Raphael2", 3, null));
        a.add(new ScrobbledArtist("manolo", 4, null));
        artistDataLinkedList.setWrapped(a);
        correctionAdder = new HashMap<>();

        for (ScrobbledArtist datum : artistDataLinkedList.getWrapped()) {
            CommandUtil.validate(dao, datum, lastFM, discogsApi, spotify);
        }
        dao.incrementalUpdate(artistDataLinkedList, "manuelk");

        assertTrue(dao.getUpdaterStatus("Raphael1").isCorrection_status());
        assertEquals(dao.getUpdaterStatus("Raphael1").getArtistUrl(), "a");
        assertNull(dao.findCorrection("Raphael1"));
        assertEquals(dao.findCorrection("Raphael4"), lastFM.getCorrection("Raphael4"));
    }


}
