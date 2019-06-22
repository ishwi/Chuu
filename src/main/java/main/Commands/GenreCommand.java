package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.AlbumInfo;
import DAO.Entities.Genre;
import DAO.Entities.UrlCapsule;
import DAO.MusicBrainz.MusicBrainzService;
import DAO.MusicBrainz.MusicBrainzServiceImpl;
import main.Exceptions.LastFmException;
import main.Parsers.TimerFrameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class GenreCommand extends ConcurrentCommand {
	private final MusicBrainzService musicBrainz;

	GenreCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new TimerFrameParser(dao);
		this.musicBrainz = new MusicBrainzServiceImpl();
	}

	@Override
	protected void threadableCode(MessageReceivedEvent e) {

		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;


		String timeframe = returned[0];
		String username = returned[1];

		BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
		try {
			lastFM.getUserList(username, timeframe, 15, 15, true, queue);
		} catch (LastFmException ex) {
			ex.printStackTrace();
			return;
		}
		List<AlbumInfo> albumInfos = queue.stream().map(capsule -> new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName())).collect(Collectors.toList());
		Map<Genre, Integer> map = musicBrainz.genreCount(albumInfos);
		StringBuilder sb = new StringBuilder();
		map.entrySet().stream().sorted(((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))).forEachOrdered(entry -> {
			Genre genre = entry.getKey();
			int plays = entry.getValue();
			sb.append("Genre: ").append(genre.getGenreName()).append(" \n")
					.append("Frequency: ").append(plays).append("\n").append("Representative ").append(genre.getRepresentativeArtist()).append("\n");
		});
		sendMessage(e, sb.toString());

	}

	@Override
	List<String> getAliases() {
		return null;
	}

	@Override
	String getDescription() {
		return null;
	}

	@Override
	String getName() {
		return null;
	}

	@Override
	List<String> getUsageInstructions() {
		return null;
	}
}
