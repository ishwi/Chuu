package main.commands;

import dao.DaoImplementation;
import dao.entities.LastFMData;
import main.exceptions.LastFmException;
import main.parsers.ArtistParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

public class ArtistPlaysCommand extends ConcurrentCommand {
	public ArtistPlaysCommand(DaoImplementation dao) {
		super(dao);
		parser = new ArtistParser(dao, lastFM);
	}

	@Override
	public String getDescription() {
		return "Gets the amount of times an user has played an specific artist";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("plays");
	}

	@Override
	public String getName() {
		return "Plays on a specific artist";
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;
		String artist = returned[0];
		artist = CommandUtil.onlyCorrection(getDao(), artist, lastFM);
		long whom = Long.parseLong(returned[1]);
		int a;

		LastFMData data = getDao().findLastFMData(whom);

		a = getDao().getArtistPlays(artist, data.getName());
		String usernameString = getUserStringConsideringGuildOrNot(e, whom, data.getName());
		String ending = a != 1 ? "times" : "time";
		sendMessageQueue(e, "**" + usernameString + "** has scrobbled **" + artist + " " + a + "**  " + ending);

	}
}
