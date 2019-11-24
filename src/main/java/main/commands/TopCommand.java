package main.commands;

import dao.DaoImplementation;
import dao.entities.UrlCapsule;
import main.exceptions.InstanceNotFoundException;
import main.exceptions.LastFmException;
import main.imagerenderer.UrlCapsuleConcurrentQueue;
import main.parsers.TopParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TopCommand extends ArtistCommand {
	public TopCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new TopParser(dao);

	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] message;
		message = parser.parse(e);
		if (message == null)
			return;

		String lastfmName = message[0];
		boolean isArtist = Boolean.parseBoolean(message[1]);

		if (!isArtist) {
			BlockingQueue<UrlCapsule> queue = new ArrayBlockingQueue<>(25);
			lastFM.getUserList(lastfmName, "overall", 5, 5, true, queue);
			generateImage(queue, 5, 5, e, true, true);

		} else {
			UrlCapsuleConcurrentQueue queue = new UrlCapsuleConcurrentQueue(getDao(), discogsApi, spotifyApi);
			lastFM.getUserList(lastfmName, "overall", 5, 5, false, queue);
			generateImage(queue, 5, 5, e, true, true);
		}
	}

	@Override
	public String getDescription() {
		return ("Your all time top albums!");
	}

	@Override
	public String getName() {
		return "Top Albums Chart";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("top");
	}
}
