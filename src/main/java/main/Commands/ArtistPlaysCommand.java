package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import main.Parsers.ArtistParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

public class ArtistPlaysCommand extends ConcurrentCommand {
	public ArtistPlaysCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ArtistParser(dao, lastFM);
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;
		String artist = returned[0];
		long whom = Long.parseLong(returned[1]);
		int a;
		try {
			LastFMData data = getDao().findLastFMData(whom);

			a = getDao().getArtistPlays(artist, data.getName());
			Member b = e.getGuild().getMemberById(whom);
			String usernameString = data.getName();
			if (b != null)
				usernameString = b.getEffectiveName();
			String ending = a > 1 ? "times " : "time";
			sendMessage(e, "**" + usernameString + "** has scrobbled **" + artist + " " + a + "**  " + ending);

		} catch (InstanceNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		}
	}

	@Override
	public String getDescription() {
		return "Gets the amount of times an user has played an specific artist";
	}

	@Override
	public String getName() {
		return "Plays on a specific artist";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!plays");
	}
}
