package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.last.ConcurrentLastFM;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UsernameAndNpQueryParser extends ArtistParser {
	public UsernameAndNpQueryParser(DaoImplementation dao, ConcurrentLastFM lastFM) {
		super(dao, lastFM);
	}

	@Override
	public String[] doSomethingWithNp(NowPlayingArtist np, User sample, MessageReceivedEvent e) {
		return new String[]{np.getArtistName() + " " + np.getSongName(), String.valueOf(e.getAuthor().getIdLong())};
	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " [whatever you want to search for] **\n" +
				"\tif you dont introduce a query takes your now playing song\n" +
				"\tyou can add a username to use that user now playing song\n";
	}
}
