package main.Commands;

import DAO.DaoImplementation;
import main.APIs.Discogs.DiscogsApi;
import main.APIs.Discogs.DiscogsSingleton;
import main.APIs.Spotify.Spotify;
import main.APIs.Spotify.SpotifySingleton;
import main.Exceptions.LastFmException;
import main.ImageRenderer.UrlCapsuleConcurrentQueue;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class ArtistCommand extends ChartCommand {
	final DiscogsApi discogsApi;
	final Spotify spotifyApi;


	public ArtistCommand(DaoImplementation dao) {
		super(dao);
		discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
		spotifyApi = SpotifySingleton.getInstanceUsingDoubleLocking();
	}

	@Override
	public void processQueue(String username, String time, int x, int y, MessageReceivedEvent e) throws LastFmException {
		UrlCapsuleConcurrentQueue queue = new UrlCapsuleConcurrentQueue(getDao(), discogsApi, spotifyApi);
		lastFM.getUserList(username, time, x, y, false, queue);
		generateImage(queue, x, y, e);
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!artist");
	}

	@Override
	public String getDescription() {
		return "Returns a Chart with artist";
	}

	@Override
	public String getName() {
		return "Artists";
	}



}
