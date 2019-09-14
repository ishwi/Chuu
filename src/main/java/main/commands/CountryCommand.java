package main.commands;

import dao.DaoImplementation;
import dao.entities.ArtistInfo;
import dao.entities.Country;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import main.exceptions.LastFmEntityNotFoundException;
import main.exceptions.LastFmException;
import main.imagerenderer.WorldMapRenderer;
import main.parsers.TimerFrameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class CountryCommand extends ConcurrentCommand {
	private final MusicBrainzService musicBrainz;

	public CountryCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new TimerFrameParser(dao, TimeFrameEnum.ALL);
		this.musicBrainz = MusicBrainzServiceSingleton.getInstance();

	}


	@Override
	List<String> getAliases() {
		return Collections.singletonList("countries");
	}

	@Override
	String getDescription() {
		return "Map representation of your scrobbled artists";
	}

	@Override
	String getName() {
		return "My Countries ";
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) {
		String[] returned = parser.parse(e);
		if (returned == null)
			return;

		String username = returned[0];
		String timeframe = returned[1];
		if (timeframe.equals(TimeFrameEnum.SEMESTER.toApiFormat()) || timeframe.equals(TimeFrameEnum.ALL.toApiFormat()))
			sendMessageQueue(e, "Going to take a while ");

		BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
		try {
			lastFM.getUserList(username, timeframe, 100, 100, false, queue);
		} catch (LastFmEntityNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(4), e);
			return;
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
			return;
		}
		List<ArtistInfo> artistInfos = queue.stream()
				.map(capsule -> new ArtistInfo(capsule.getUrl(), capsule.getArtistName(), capsule.getMbid()))
				.filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
				.collect(Collectors.toList());

		Map<Country, Integer> map = musicBrainz.countryCount(artistInfos);

		if (map.isEmpty()) {
			sendMessageQueue(e, "Was not able to find any country ");
			return;
		}

		byte[] b = WorldMapRenderer.generateImage(map);
		if (b == null) {
			parser.sendError("Unknown error happened while creating the map", e);
			return;
		}

		if (b.length < 8388608)
			e.getChannel().sendFile(b, "cat.png").queue();
			//messageBuilder.sendTo(e.getChannel()).addFile(img, "cat.png").queue();
		else
			e.getChannel().sendMessage("Boot too big").queue();

	}
}
