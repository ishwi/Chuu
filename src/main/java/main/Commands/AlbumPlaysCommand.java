package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.LastFMData;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Discogs.DiscogsSingleton;
import main.APIs.Spotify.Spotify;
import main.APIs.Spotify.SpotifySingleton;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.Parsers.ArtistAlbumParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.List;


public class AlbumPlaysCommand extends ConcurrentCommand {
	private final DiscogsApi discogsApi;
	private final Spotify spotify;

	public AlbumPlaysCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ArtistAlbumParser(dao, lastFM);
		this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("album");
	}

	void doSomethingWithAlbumArtist(String artist, String album, MessageReceivedEvent e, long who) {
		int a;
		try {
			LastFMData data = getDao().findLastFMData(who);

			a = lastFM.getPlaysAlbum_Artist(data.getName(), artist, album).getPlays();
			String usernameString = data.getName();

			usernameString = getUserStringConsideringGuildOrNot(e, who, usernameString);

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
	public void onCommand(MessageReceivedEvent e) {
		String[] parsed;
		parsed = parser.parse(e);
		if (parsed == null || parsed.length != 3)
			return;
		String artist = parsed[0];
		String album = parsed[1];
		long whom = Long.parseLong(parsed[2]);
		artist = CommandUtil.onlyCorrection(getDao(), artist, lastFM);
		doSomethingWithAlbumArtist(artist, album, e, whom);

	}


}
