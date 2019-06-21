package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.AlbumInfo;
import DAO.Entities.UrlCapsule;
import DAO.MusicBrainz.MusicBrainzService;
import DAO.MusicBrainz.MusicBrainzServiceImpl;
import main.Exceptions.LastFmException;
import main.Parsers.ChartParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MusicBrainzCommand extends ArtistCommand {
	private final MusicBrainzService mb = new MusicBrainzServiceImpl();

	public MusicBrainzCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ChartParser(dao);//

	}

	@Override
	public void processQueue(String username, String time, int x, int y, MessageReceivedEvent e) throws LastFmException {
		BlockingQueue<UrlCapsule> queue = new LinkedBlockingDeque<>();
		lastFM.getUserList(username, time, x, y, true, queue);
		List<AlbumInfo> albumInfos = queue.stream().map(capsule -> new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName())).collect(Collectors.toList());
		List<AlbumInfo> finalAlbumInfos = mb.listOfCurrentYear(albumInfos);

		AtomicInteger counter2 = new AtomicInteger(0);
		queue.removeIf(urlCapsule -> {
			for (AlbumInfo albumInfo : finalAlbumInfos) {
				if (albumInfo.getMbid().equals(urlCapsule.getMbid())) {
					urlCapsule.setPos(counter2.getAndAdd(1));
					return false;
				}
			}
			return true;
		});

		generateImage(queue, (int) Math.sqrt(queue.size()) + 1, y, e);
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!mbiz");
	}

	@Override
	public String getDescription() {
		return "mbiz";
	}

	@Override
	public String getName() {
		return "Band";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!band artist\n\t --image for Image format\n\n"
		);
	}
}
