package main.scheduledtasks;

import dao.DaoImplementation;
import dao.SimpleDataSource;
import dao.entities.ArtistData;
import dao.entities.ArtistInfo;
import dao.entities.LastFMData;
import dao.entities.TimestampWrapper;
import main.apis.discogs.DiscogsApi;
import main.apis.last.ConcurrentLastFM;
import main.apis.last.LastFMFactory;
import main.apis.spotify.Spotify;
import main.commands.CommandUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.util.*;

import static org.junit.Assert.*;


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
			PreparedStatement preparedStatement = connection.prepareStatement("TRUNCATE `lastfm_test`.`artist_url`");
			preparedStatement.execute();
			preparedStatement = connection.prepareStatement("TRUNCATE `lastfm_test`.`corrections`");
			preparedStatement.execute();
		}

	}

	@Before
	public void setUp() {

		dataSource = new SimpleDataSource("/datasource.properties");
		discogsApi = new DiscogsMockup();
		spotify = null;
		lastFM = LastFMFactory.getNewInstance();
		dao = new DaoImplementation(dataSource);
		dao.insertArtistDataList(new LastFMData("manuelk", 1));
		dao.insertArtistDataList(Collections.emptyList(), "manuelk");
	}

	@Test
	public void run() {
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

		lastFM = new lastFMMockup("apikey");
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
		assertTrue(dao.getUpdaterStatus("Rapahel1").isCorrection_status());
		assertEquals(dao.getUpdaterStatus("Rapahel1").getArtistUrl(), "a");
		assertNull(dao.findCorrection("Raphael1"));
		assertEquals(dao.findCorrection("Raphael4"), lastFM.getCorrection("Raphael4"));
	}

	static class lastFMMockup extends ConcurrentLastFM {
		lastFMMockup(String apikey) {
			super(apikey);
		}

		@Override
		public String getCorrection(String artistToCorrect) {
			return "axdasd" + artistToCorrect + "ax" + LocalDateTime.now().getDayOfYear();
		}
	}

	static class DiscogsMockup extends main.apis.discogs.DiscogsApi {

		public DiscogsMockup(String secret, String key) {
			super(secret, key);
		}

		DiscogsMockup() {
			super("a", "b");
		}

		@Override
		public Year getYearRelease(String album, String artist) {
			return Year.now();
		}

		@Override
		public String findArtistImage(String artist) {
			return "a";
		}
	}
}
