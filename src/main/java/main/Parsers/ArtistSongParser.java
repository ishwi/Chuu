package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.last.ConcurrentLastFM;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistSongParser extends ArtistAlbumParser {
	public ArtistSongParser(DaoImplementation dao, ConcurrentLastFM lastFM) {
		super(dao, lastFM);
	}

	@Override
	String[] doSomethingWithNp(NowPlayingArtist np, Member ignored, MessageReceivedEvent e) {
		return new String[]{np.getArtistName(), np.getSongName(), String.valueOf(e.getAuthor().getIdLong())};
	}


}
