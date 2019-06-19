package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.APIs.Spotify.Spotify;
import main.APIs.Spotify.SpotifySingleton;
import main.Parsers.Parser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NPSpotifyCommand extends NpCommand {
	private final Spotify spotify;

	public NPSpotifyCommand(DaoImplementation dao) {
		super(dao);
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();


	}

	@Override
	public void doSomethingWithArtist(NowPlayingArtist nowPlayingArtist, Parser parser, MessageReceivedEvent e) {
		MessageBuilder messageBuilder = new MessageBuilder();
		String uri = spotify.searchItems(nowPlayingArtist.getSongName(), nowPlayingArtist.getArtistName(), nowPlayingArtist.getAlbumName());

		if (uri.equals("")) {
			sendMessage(e, "Was not able to find artist " + nowPlayingArtist.getSongName() + " on spotify");
			return;
		}
		messageBuilder.setContent(uri).sendTo(e.getChannel()).queue();
	}






	@Override
	public List<String> getAliases() {
		return Arrays.asList("!npspotify", "!spotify", "!nps", "!npspo");
	}

	@Override
	public String getDescription() {
		return "Returns a link to your current song via Spotify";
	}

	@Override
	public String getName() {
		return "Now Playing Spotify";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!npspotify *username  \n \tIf not specified another user it defaults to yours");
	}


}
