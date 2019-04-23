package main.Commands;

import DAO.DaoImplementation;
import main.Exceptions.LastFmUserNotFoundException;
import main.Exceptions.ParseException;
import main.last.ConcurrentLastFM;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

// usage !command "song" "/@user"
public class AlbumSongPlaysCommand extends ConcurrentCommand {
	public AlbumSongPlaysCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void threadableCode() {
		String[] parsed;
		try {
			parsed = parse(e);
		} catch (ParseException e1) {
			errorMessage(e, 1, e1.getMessage());
			return;
		}
		try {
			int a = ConcurrentLastFM.getPlaysAlbum_Artist(getDao().findShow(e.getAuthor().getIdLong()).getName(), true, parsed[0], parsed[1]);
			sendMessage(e, "**" + e.getGuild().getMemberById(e.getAuthor().getIdLong()).getEffectiveName() + "** has listened " + a + " times the album **" + parsed[1] + "** by **" + parsed[0] + "**!");
		} catch (LastFmUserNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstanceNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!album");
	}

	@Override
	public String getDescription() {
		return ("How many times you have heard an album!");
	}

	@Override
	public String getName() {
		return "Get Plays Album";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList
				("**!top username**\n" + "\tIf username is not specified defaults to authors account \n\n");

	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {

		String message = e.getMessage().getContentRaw();

		String[] submessage = getSubMessage(e.getMessage());
		StringBuilder builder = new StringBuilder();
		String username;
		for (String s : submessage) {
			if (s.startsWith("@")) {
				username = s;
				continue;
			}
			builder.append(s).append(" ");
		}
		String s = builder.toString();
		String[] content = s.split("\\s*-\\s*");
		if (content.length != 2) {
			throw new ParseException("-");
		}
		String artist = content[0].trim();
		String album = content[1].trim();

		return new String[]{artist, album};
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {

	}
}
