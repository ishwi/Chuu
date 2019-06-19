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
	private final DiscogsApi discogsApi;
	private final Spotify spotifyApi;


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

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!artists *[w,m,t,y,a] *Username *SizeXSize \n" +
				"\tIf time is not specified defaults to Weekly \n" +
				"\tIf username is not specified defaults to authors account \n" +
				"\tIf size is not specified defaults to 5x5 (As big as discord lets\n\n"
		);
	}


}
