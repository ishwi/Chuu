package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import main.Spotify;
import main.last.ConcurrentLastFM;
import main.last.LastFMServiceException;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NPSpotifyCommand extends MyCommandDbAndSpotifyAccess {
	public NPSpotifyCommand(DaoImplementation dao, Spotify spotify) {
		super(dao, spotify);

	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		String username;
		MessageBuilder messageBuilder = new MessageBuilder();

		try {
			username = parse(e)[0];
		} catch (ParseException ex) {
			sendMessage(e, "aaa");
			return;
		}
		try {
			NowPlayingArtist nowPlayingArtist = ConcurrentLastFM.getNowPlayingInfo(username);
			StringBuilder a = new StringBuilder();
			e.getChannel().sendTyping().queue();
			String uri = spotify.searchItems(nowPlayingArtist.getSongName(), nowPlayingArtist.getArtistName(), nowPlayingArtist.getAlbumName());
			messageBuilder.setContent(uri).sendTo(e.getChannel()).queue();
		} catch (LastFMServiceException ex) {
			onLastFMError(e);
		}

	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("!npspotify", "!spotify", "nps", "npspo");
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
		return Collections.singletonList("If not specified another user it defaults to yours");
	}

	@Override
	public String[] parse(MessageReceivedEvent e) throws ParseException {
		return new String[]{getLastFmUsername1input(getSubMessage(e.getMessage()), e.getAuthor().getIdLong(), e)};
	}
}
