package main.commands;

import dao.DaoImplementation;
import dao.entities.NowPlayingArtist;
import main.apis.Spotify.Spotify;
import main.apis.Spotify.SpotifySingleton;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class NPSpotifyCommand extends NpCommand {
	private final Spotify spotify;

	public NPSpotifyCommand(DaoImplementation dao) {
		super(dao);
		this.spotify = SpotifySingleton.getInstanceUsingDoubleLocking();


	}

	@Override
	public void doSomethingWithArtist(NowPlayingArtist nowPlayingArtist, MessageReceivedEvent e) {
		MessageBuilder messageBuilder = new MessageBuilder();
		String uri = spotify
				.searchItems(nowPlayingArtist.getSongName(), nowPlayingArtist.getArtistName(), nowPlayingArtist
						.getAlbumName());

		if (uri.equals("")) {
			sendMessageQueue(e, "Was not able to find " + nowPlayingArtist.getArtistName() + " - " + nowPlayingArtist
					.getSongName() + " on spotify");
			return;
		}
		messageBuilder.setContent(uri).sendTo(e.getChannel()).queue();
	}

	@Override
	public String getDescription() {
		return "Returns a link to your current song via Spotify";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("npspotify", "spotify", "nps", "npspo");
	}

	@Override
	public String getName() {
		return "Now Playing Spotify";
	}


}
