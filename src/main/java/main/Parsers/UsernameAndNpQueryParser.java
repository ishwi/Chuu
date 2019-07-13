package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.last.ConcurrentLastFM;
import net.dv8tion.jda.api.entities.Member;

import java.util.Collections;
import java.util.List;

public class UsernameAndNpQueryParser extends ArtistParser {
	public UsernameAndNpQueryParser(DaoImplementation dao, ConcurrentLastFM lastFM) {
		super(dao, lastFM);
	}

	@Override
	public String[] doSomethingWithNp(NowPlayingArtist np, Member sample) {
		return new String[]{np.getArtistName() + " " + np.getSongName(), String.valueOf(sample.getIdLong())};
	}

	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " [whatever you want to search for] **\n" +
				"\tif you dont introduce a query takes your now playing song\n" +
				"\tyou can add a username to use that user now playing song\n\n");
	}
}
