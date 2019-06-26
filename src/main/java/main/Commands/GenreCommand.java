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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class GenreCommand extends ConcurrentCommand {
	private final MusicBrainzService musicBrainz;

	public GenreCommand(DaoImplementation dao) {
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


		String username = returned[0];
		String timeframe = returned[1];

		BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
		try {
			lastFM.getUserList(username, timeframe, 15, 15, true, queue);
		} catch (LastFmException ex) {
			ex.printStackTrace();
			return;
		}
		List<AlbumInfo> albumInfos = queue.stream().map(capsule -> new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName())).collect(Collectors.toList());
		List<AlbumInfo> mbizList =
				albumInfos.stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty()).collect(Collectors.toList());
		Map<Genre, Integer> map = musicBrainz.genreCount(mbizList);
		StringBuilder sb = new StringBuilder();
		map.entrySet().stream().sorted(((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))).sequential().limit(20).forEachOrdered(entry -> {
			Genre genre = entry.getKey();
			int plays = entry.getValue();
			sb.append("Genre: ").append(genre.getGenreName()).append(" \n")
					.append("Frequency: ").append(plays).append("\n").append("Representative ").append(genre.getRepresentativeArtist()).append("\n");
		});
		sendMessage(e, sb.toString());

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!genre");
	}

	@Override
	public String getDescription() {
		return "genre list";
	}

	@Override
	public String getName() {
		return "Genre";
	}


}
