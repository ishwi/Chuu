package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Parsers.ArtistAlbumParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;


public class AlbumSongPlaysCommand extends ConcurrentCommand {
	public AlbumSongPlaysCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ArtistAlbumParser(dao, lastFM);
	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] parsed;
		parsed = parser.parse(e);
		if (parsed == null || parsed.length != 3)
			return;
		String artist = parsed[0];
		String album = parsed[1];
		long whom = Long.parseLong(parsed[2]);

		doSomethingWithAlbumArtist(artist, album, e, whom);

	}

	void doSomethingWithAlbumArtist(String artist, String album, MessageReceivedEvent e, long who) {
		int a;
		try {
			LastFMData data = getDao().findLastFMData(who);

			a = lastFM.getPlaysAlbum_Artist(data.getName(), artist, album).getPlays();
			Member b = e.getGuild().getMemberById(who);
			String usernameString = data.getName();
			if (b != null)
				usernameString = b.getEffectiveName();
			String ending = a > 1 ? "times " : "time";

			sendMessage(e, "**" + usernameString + "** has listened **" + album + "** " + a + " " + ending);

		} catch (InstanceNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(5), e);
		} catch (LastFmEntityNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(6), e);
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
		}
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
	public List<String> getAliases() {
		return Collections.singletonList("!album");
	}


}
