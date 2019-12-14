package core.commands;

import dao.DaoImplementation;
import dao.entities.AlbumInfo;
import dao.entities.UrlCapsule;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ChartFromYearParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MusicBrainzCommand extends ArtistCommand {
	public static final int chartSize = 100;
	private final MusicBrainzService mb;


	public MusicBrainzCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ChartFromYearParser(dao);//

		mb = MusicBrainzServiceSingleton.getInstance();
	}

	@Override
	public String getName() {
		return "Released in YEAR";
	}

	@Override
	public String getDescription() {
		return "Gets your top albums from the time frame provided and check if they were released in the provided year";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("releaseyear");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;

		Year year = Year.of(Integer.parseInt(returned[1]));
		String username = returned[2];
		String time = returned[3];
		boolean titleWrite = !Boolean.parseBoolean(returned[5]);
		boolean playsWrite = Boolean.parseBoolean(returned[6]);

		int x = (int) Math.sqrt(chartSize);

		calculateYearAlbums(username, time, chartSize, x, x, year, e, titleWrite, playsWrite, false);


	}

	public void calculateYearAlbums(String username, String time, int numberOfAlbumsToQueryFor, int x, int y, Year year, MessageReceivedEvent e, boolean writeTiles, boolean writePlays, boolean caresAboutSize) throws LastFmException {
		BlockingQueue<UrlCapsule> queue = new LinkedBlockingDeque<>();

		lastFM.getUserList(username, time, numberOfAlbumsToQueryFor, 1, true, queue);
		//List of obtained elements
		Map<Boolean, List<AlbumInfo>> results =
				queue.stream()
						.map(capsule ->
								new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName()))
						.collect(Collectors.partitioningBy(albumInfo -> albumInfo.getMbid().isEmpty()));

		List<AlbumInfo> nonEmptyMbid = results.get(false);
		List<AlbumInfo> emptyMbid = results.get(true);

		//List<AlbumInfo> nullYearList = new ArrayList<>();
		List<AlbumInfo> albumsMbizMatchingYear = mb.listOfYearReleases(nonEmptyMbid, year);
		List<AlbumInfo> mbFoundBYName = mb.findArtistByRelease(emptyMbid, year);
		emptyMbid.removeAll(mbFoundBYName);
		int discogsMetrics = 0;
		if (doDiscogs()) {
			List<AlbumInfo> foundDiscogsMatchingYear = emptyMbid.stream().filter(albumInfo -> {
				try {

					Year tempYear = (discogsApi.getYearRelease(albumInfo.getName(), albumInfo.getArtist()));
					if (tempYear == null) {
						//nullYearList.add(albumInfo);
						return false;
					}
					return tempYear.equals(year);
				} catch (Exception ex) {
					//Chuu.getLogger().warn(e.getMessage(), e);
					//nullYearList.add(albumInfo);
					return false;
				}
			}).collect(Collectors.toList());
			albumsMbizMatchingYear.addAll(foundDiscogsMatchingYear);
			discogsMetrics = foundDiscogsMatchingYear.size();
		}
		albumsMbizMatchingYear.addAll(mbFoundBYName);

		//Keep the order of the original queue so the final chart is ordered by plays
		AtomicInteger counter2 = new AtomicInteger(0);
		queue.removeIf(urlCapsule -> {
			for (AlbumInfo albumInfo : albumsMbizMatchingYear) {
				if ((!albumInfo.getMbid().isEmpty() && albumInfo.getMbid().equals(urlCapsule.getMbid())) || urlCapsule
						.getAlbumName().equalsIgnoreCase(albumInfo.getName()) && urlCapsule.getArtistName()
						.equalsIgnoreCase(albumInfo.getArtist())) {
					urlCapsule.setPos(counter2.getAndAdd(1));
					return false;
				}
			}
			return true;
		});

		if (queue.isEmpty()) {
			sendMessageQueue(e, "Dont have any " + year.toString() + " album in your top " + numberOfAlbumsToQueryFor + " albums");
			return;
		}
		if (!caresAboutSize) {
			int imageSize = (int) Math.ceil(Math.sqrt(queue.size()));
			generateImage(queue, imageSize, imageSize, e, writeTiles, writePlays);
		} else {
			BlockingQueue<UrlCapsule> tempQueuenew = new LinkedBlockingDeque<>();
			queue.drainTo(tempQueuenew, x * y);
			generateImage(tempQueuenew, x, y, e, writeTiles, writePlays);
		}

		getDao().updateMetrics(discogsMetrics, mbFoundBYName.size(), albumsMbizMatchingYear
				.size(), ((long) x)  * x);

	}

	public boolean doDiscogs() {
		return true;

	}


}
