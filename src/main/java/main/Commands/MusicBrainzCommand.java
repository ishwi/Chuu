package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.AlbumInfo;
import DAO.Entities.UrlCapsule;
import DAO.MusicBrainz.MusicBrainzService;
import DAO.MusicBrainz.MusicBrainzServiceImpl;
import main.Exceptions.DiscogsServiceException;
import main.Exceptions.LastFmException;
import main.Parsers.ChartFromYearParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MusicBrainzCommand extends ArtistCommand {
	private final MusicBrainzService mb = new MusicBrainzServiceImpl();

	public MusicBrainzCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ChartFromYearParser(dao, 100);//

	}

	@Override
	public void processQueue(String username, String time, int chartSize, int yearInt, MessageReceivedEvent e) throws LastFmException {
		BlockingQueue<UrlCapsule> queue = new LinkedBlockingDeque<>();
		int x = (int) Math.sqrt(chartSize);
		Year year = Year.of(yearInt);
		lastFM.getUserList(username, time, x, x, true, queue);
		//List of obtained elements
		List<AlbumInfo> albumInfos = queue.stream().map(capsule -> new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName())).collect(Collectors.toList());


		Map<Boolean, List<AlbumInfo>> results =
				albumInfos.stream().collect(Collectors.partitioningBy(albumInfo -> albumInfo.getMbid().isEmpty()));

		List<AlbumInfo> nonEmptyMbid = results.get(false);
		List<AlbumInfo> emptyMbid = results.get(true);

		List<AlbumInfo> nullYearList = new ArrayList<>();
		List<AlbumInfo> albumsMbizMatchingYear = mb.listOfYearReleases(nonEmptyMbid, year);
		List<AlbumInfo> foundDiscogsMatchingYear = emptyMbid.stream().filter(albumInfo -> {
			try {

				Year tempYear = (discogsApi.getYearRelease(albumInfo.getName(), albumInfo.getArtist()));
				if (tempYear == null) {
					nullYearList.add(albumInfo);
					return false;
				}
				return tempYear.equals(year);
			} catch (DiscogsServiceException ex) {
				ex.printStackTrace();
				return false;
			}
		}).collect(Collectors.toList());
		albumsMbizMatchingYear.addAll(mb.findArtistByRelease(nullYearList, year));
		albumsMbizMatchingYear.addAll(foundDiscogsMatchingYear);


		AtomicInteger counter2 = new AtomicInteger(0);
		queue.removeIf(urlCapsule -> {
			for (AlbumInfo albumInfo : albumsMbizMatchingYear) {
				if ((!albumInfo.getMbid().isEmpty() && albumInfo.getMbid().equals(urlCapsule.getMbid())) || urlCapsule.getAlbumName().equalsIgnoreCase(albumInfo.getName()) && urlCapsule.getArtistName().equalsIgnoreCase(albumInfo.getArtist())) {
					urlCapsule.setPos(counter2.getAndAdd(1));
					return false;
				}
			}
			return true;
		});
		if (queue.isEmpty()) {
			sendMessage(e, "Dont have  any " + year.toString() + "album in your top " + chartSize + "albums");
			return;
		}
		int imagesize = (int) Math.sqrt(queue.size());
		generateImage(queue, imagesize, imagesize, e);
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!mbiz");
	}

	@Override
	public String getDescription() {
		return "Gets your top albums from the time frame provided and check if they were released in the provided year";
	}

	@Override
	public String getName() {
		return "mbiz";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("**!mbiz *[w,m,q,s,y,a] *Username YEAR** \n" +
				"\tIf time is not specified defaults to Yearly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf Year not specified it default to current year\n\n"
		);
	}
}
