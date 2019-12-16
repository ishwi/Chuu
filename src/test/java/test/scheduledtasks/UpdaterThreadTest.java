package test.scheduledtasks;

import core.apis.discogs.DiscogsApi;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.spotify.Spotify;
import core.commands.CommandUtil;
import core.exceptions.DiscogsServiceException;
import dao.DaoImplementation;
import dao.SimpleDataSource;
import dao.entities.ArtistData;
import dao.entities.ArtistInfo;
import dao.entities.LastFMData;
import dao.entities.TimestampWrapper;
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


public class UpdaterThreadTest {
	private Spotify spotify;
	private DaoImplementation dao;
	private DiscogsApi discogsApi;
	private ConcurrentLastFM lastFM;
	private SimpleDataSource dataSource;

	@After
	public void tearDown() throws Exception {
		dao.removeUserCompletely(1L);
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("TRUNCATE `lastfm_test`.`corrections`");
			preparedStatement.execute();
			preparedStatement = connection
					.prepareStatement("Delete from `lastfm_test`.`artist_url` where artist_id like 'Raphael%' or artist_id = 'manolo'");
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

		dao = new DaoImplementation(dataSource);
		dao.insertArtistDataList(new LastFMData("manuelk", 1));
		dao.insertArtistDataList(Collections.emptyList(), "manuelk");
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("TRUNCATE `lastfm_test`.`corrections`");
			preparedStatement.execute();
			preparedStatement = connection
					.prepareStatement("Delete from `lastfm_test`.`artist_url` where artist_id like 'Raphael%' or artist_id = 'manolo'");
			preparedStatement.execute();
		}

	}

	@Test
	public void run() throws DiscogsServiceException {
		Map<ArtistData, String> correctionAdder = new HashMap<>();

		List<ArtistData> a = new ArrayList<>();
		a.add(new ArtistData("Raphael", 1, null));
		a.add(new ArtistData("Raphael1", 2, null));
		a.add(new ArtistData("Raphael2", 3, null));
		a.add(new ArtistData("Raphael3", 4, null));

		TimestampWrapper<List<ArtistData>> artistDataLinkedList = new TimestampWrapper<>(a, Instant.now()
				.get(ChronoField.MILLI_OF_SECOND) * 1000);
		//Correction with current last fm implementation should return the same name so no correction gives
		for (ArtistData datum : artistDataLinkedList.getWrapped()) {
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
		a.add(new ArtistData("Raphael4", 1, null));
		a.add(new ArtistData("Raphael1", 2, null));
		a.add(new ArtistData("Raphael2", 3, null));
		a.add(new ArtistData("manolo", 4, null));
		artistDataLinkedList.setWrapped(a);
		correctionAdder = new HashMap<>();

		for (ArtistData datum : artistDataLinkedList.getWrapped()) {
			CommandUtil.valiate(dao, datum, lastFM, discogsApi, spotify, correctionAdder);
		}
		dao.incrementalUpdate(artistDataLinkedList, "manuelk");
		correctionAdder.forEach((artistData, s) -> dao.insertCorrection(s, artistData.getArtist()));
		assertTrue(dao.getUpdaterStatus("Raphael1").isCorrection_status());
		assertEquals(dao.getUpdaterStatus("Raphael1").getArtistUrl(), "a");
		assertNull(dao.findCorrection("Raphael1"));
		assertEquals(dao.findCorrection("Raphael4"), lastFM.getCorrection("Raphael4"));
	}


}
