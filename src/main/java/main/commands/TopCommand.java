package main.commands;

import dao.DaoImplementation;
import dao.entities.UrlCapsule;
import main.exceptions.LastFMNoPlaysException;
import main.exceptions.LastFmEntityNotFoundException;
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
	public void onCommand(MessageReceivedEvent e) {
		String[] message;
		message = parser.parse(e);
		if (message == null)
			return;

		String lastfmName = message[0];
		boolean isArtist = Boolean.parseBoolean(message[1]);
		try {

			if (!isArtist) {
				BlockingQueue<UrlCapsule> queue = new ArrayBlockingQueue<>(25);
				lastFM.getUserList(lastfmName, "overall", 5, 5, true, queue);
				generateImage(queue, 5, 5, e, true, true);

			} else {
				UrlCapsuleConcurrentQueue queue = new UrlCapsuleConcurrentQueue(getDao(), discogsApi, spotifyApi);
				lastFM.getUserList(lastfmName, "overall", 5, 5, false, queue);
				generateImage(queue, 5, 5, e, true, true);
			}
		} catch (LastFMNoPlaysException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmEntityNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(4), e);
		} catch (LastFmException ex2) {
			parser.sendError(parser.getErrorMessage(2), e);
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
