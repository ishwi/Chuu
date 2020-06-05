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

    }
    @Test
    public void run() throws DiscogsServiceException {
    }

}
