package main.parsers;

import dao.DaoImplementation;
import dao.entities.NowPlayingArtist;
import main.apis.last.ConcurrentLastFM;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistSongParser extends ArtistAlbumParser {
	public ArtistSongParser(DaoImplementation dao, ConcurrentLastFM lastFM) {
		super(dao, lastFM);
	}

	@Override
	String[] doSomethingWithNp(NowPlayingArtist np, User ignored, MessageReceivedEvent e) {
		return new String[]{np.getArtistName(), np.getSongName(), String.valueOf(e.getAuthor().getIdLong())};
	}


}
