package main.Commands;

import DAO.DaoImplementation;
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
		String userName = parsed[2];


		doSomethingWithAlbumArtist(artist, album, e, userName);

	}

	void doSomethingWithAlbumArtist(String artist, String album, MessageReceivedEvent e, String who) {
		int a;
		try {
			a = lastFM.getPlaysAlbum_Artist(getDao().findLastFMData(e.getAuthor().getIdLong()).getName(), artist, album).getPlays();
			Member b = e.getGuild().getMemberById(e.getAuthor().getIdLong());
			if (b != null)
				who = b.getEffectiveName();
			sendMessage(e, "**" + who + "** has listened " + a + " times the album **" + album + "** by **" + artist + "**!");

		} catch (InstanceNotFoundException | LastFmEntityNotFoundException ex) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
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
}
