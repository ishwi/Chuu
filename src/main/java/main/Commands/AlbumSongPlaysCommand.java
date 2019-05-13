package main.Commands;

import DAO.DaoImplementation;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Exceptions.ParseException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;

// usage !command "song" "/@user"
//Right now only for author
public class AlbumSongPlaysCommand extends ConcurrentCommand {
	public AlbumSongPlaysCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] parsed;
		try {
			parsed = parse(e);
		} catch (ParseException e1) {
			errorMessage(e, 1, e1.getMessage());
			return;
		}
		try {
			int a = lastFM.getPlaysAlbum_Artist(getDao().findShow(e.getAuthor().getIdLong()).getName(), true, parsed[0], parsed[1]);
			sendMessage(e, "**" + e.getGuild().getMemberById(e.getAuthor().getIdLong()).getEffectiveName() + "** has listened " + a + " times the album **" + parsed[1] + "** by **" + parsed[0] + "**!");
		} catch (LastFmEntityNotFoundException | InstanceNotFoundException e1) {
			errorMessage(e, 2, "");
		} catch (LastFmException ex) {
			errorMessage(e, 3, "");
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
				("!album artist-album \n\n");

	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {


		String[] submessage = getSubMessage(e.getMessage());
		StringBuilder builder = new StringBuilder();
		for (String s : submessage) {
			builder.append(s).append(" ");
		}
		String s = builder.toString();
		String[] content = s.split("\\s*-\\s*");
		if (content.length != 2) {
			throw new ParseException("-");
		}
		String artist = content[0].trim();
		String album = content[1].trim();

		return new String[]{artist, album,};
	}

	@Override
	public void errorMessage(MessageReceivedEvent e, int code, String cause) {
		String base = " An Error Happened while processing " + e.getAuthor().getName() + "'s request:\n";
		String message;

		switch (code) {

			case 1:
				message = "Please separete artist and album with a \"-\"";
				break;
			case 2:
				message = "Artist not found!";
				break;
			default:
				message = "Internal Server Error, Try again later";


		}
		sendMessage(e, base + message);
	}
}
