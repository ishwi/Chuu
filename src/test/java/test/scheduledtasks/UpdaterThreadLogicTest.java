package test.scheduledtasks;

import core.apis.discogs.DiscogsApi;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import dao.ChuuService;
import dao.MbizDatasource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;


public class UpdaterThreadLogicTest {
    private Spotify spotify;
    private ChuuService dao;
    private DiscogsApi discogsApi;
    private ConcurrentLastFM lastFM;
    private MbizDatasource dataSource;

    @AfterEach
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

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void run() {
    }

}
