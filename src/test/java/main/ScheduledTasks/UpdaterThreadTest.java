package main.ScheduledTasks;

import DAO.DaoImplementation;
import DAO.Entities.ArtistData;
import DAO.Entities.ArtistInfo;
import DAO.Entities.LastFMData;
import DAO.Entities.TimestampWrapper;
import DAO.SimpleDataSource;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Spotify.Spotify;
import main.APIs.last.ConcurrentLastFM;
import main.Commands.CommandUtil;
import main.Exceptions.DiscogsServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.util.*;


public class UpdaterThreadTest {
	private Spotify spotify;
	private DaoImplementation dao;
	private DiscogsApi discogsApi;
	private ConcurrentLastFM lastFM;

	@After
	public void tearDown() throws Exception {
		dao.removeUserCompletely((long) 1);
	}

	@Before
	public void setUp() throws Exception {


		DataSource dataSource = new SimpleDataSource("/datasource.properties");
		discogsApi = new pene();
		spotify = null;
		lastFM = new ConcurrentLastFM();
		dao = new DaoImplementation(dataSource);
		dao.addUser(new LastFMData("manuelk", 1));
		dao.addUser(Collections.emptyList(), "manuelk");
	}

	@Test
	public void run() {
		Map<ArtistData, String> correctionAdder = new HashMap<>();

		List<ArtistData> a = new ArrayList<>();
		a.add(new ArtistData("Raphael", 1, null));
		a.add(new ArtistData("Raphael1", 2, null));
		a.add(new ArtistData("Raphael2", 3, null));
		a.add(new ArtistData("Raphael3", 4, null));

		TimestampWrapper<List<ArtistData>> artistDataLinkedList = new TimestampWrapper<>(a, Instant.now().get(ChronoField.MILLI_OF_SECOND));
		for (ArtistData datum : artistDataLinkedList.getWrapped()) {
			CommandUtil.valiate(dao, datum, lastFM, discogsApi, spotify, correctionAdder);
		}

		dao.incrementalUpdate(artistDataLinkedList, "manuelk");
		correctionAdder.forEach((artistData, s) -> dao.insertCorrection(s, artistData.getArtist()));


		dao.upsertUrl(new ArtistInfo("b", "manolo"));

		dao.createCorrection("Raphael1", "manolo");
		dao.createCorrection("Raphael2", "Raphael");
		lastFM = new lastFMMockup();
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
		correctionAdder.forEach((artistData, s) -> dao.insertCorrection(s, artistData.getArtist()));


		dao.incrementalUpdate(artistDataLinkedList, "manuelk");

	}

	class lastFMMockup extends ConcurrentLastFM {
		@Override
		public String getCorrection(String artistToCorrect) {
			return "axdasd" + artistToCorrect + "ax" + LocalDateTime.now().toString();
		}
	}

	class pene extends main.APIs.Discogs.DiscogsApi {

		public pene(String secret, String key) {
			super(secret, key);
		}

		public pene() {
			super("a", "b");
		}

		@Override
		public Year getYearRelease(String album, String artist) throws DiscogsServiceException {
			return Year.now();
		}

		@Override
		public String findArtistImage(String artist) throws DiscogsServiceException {
			return "a";
		}
	}
}
