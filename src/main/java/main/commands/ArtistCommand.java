package main.commands;

import dao.DaoImplementation;
import main.apis.discogs.DiscogsApi;
import main.apis.discogs.DiscogsSingleton;
import main.apis.spotify.Spotify;
import main.apis.spotify.SpotifySingleton;
import main.exceptions.LastFmException;
import main.imagerenderer.UrlCapsuleConcurrentQueue;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
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
	public String getDescription() {
		return "Returns a Chart with artist";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("artchart", "charta", "artistchart");
	}

	@Override
	public void processQueue(String username, String time, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays) throws LastFmException {
		UrlCapsuleConcurrentQueue queue = new UrlCapsuleConcurrentQueue(getDao(), discogsApi, spotifyApi);
		lastFM.getUserList(username, time, x, y, false, queue);
		generateImage(queue, x, y, e, writeTitles, writePlays);
	}

	@Override
	public String getName() {
		return "Artists";
	}


}
