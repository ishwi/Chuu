package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.last.ConcurrentLastFM;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class ArtistParser extends ArtistAlbumParser {

	public ArtistParser(DaoImplementation dao, ConcurrentLastFM lastFM) {
		super(dao, lastFM);
	}

	@Override
	public String[] doSomethingWithNp(NowPlayingArtist np, Member sample) {
		return new String[]{np.getArtistName(), String.valueOf(sample.getIdLong())};
	}

	@Override
	public String[] doSomethingWithString(String[] subMessage, Member sample, MessageReceivedEvent e) {

		return new String[]{artistMultipleWords(subMessage), String.valueOf(sample.getIdLong())};
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *artist*** \n\n");

	}

	@Override
	public void setUpErrorMessages() {
		errorMessages.put(2, "Internal Server Error, try again later");
	}
}
