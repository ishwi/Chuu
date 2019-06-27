package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UrlCapsule;
import main.Exceptions.LastFmEntityNotFoundException;
import main.Exceptions.LastFmException;
import main.ImageRenderer.UrlCapsuleConcurrentQueue;
import main.Parsers.TopParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
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
	public void threadableCode(MessageReceivedEvent e) {
		String[] message;
		MessageBuilder mes = new MessageBuilder();
		EmbedBuilder embed = new EmbedBuilder();
		message = parser.parse(e);
		if (message == null)
			return;

//		embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
//				.setDescription(e.getAuthor().getName() + " 's most listened albums");
//		mes.setEmbed(embed.build());
		String lastfmName = message[0];
		boolean isArtist = Boolean.parseBoolean(message[1]);
		try {

			if (!isArtist) {
				BlockingQueue<UrlCapsule> queue = new ArrayBlockingQueue<>(25);
				lastFM.getUserList(lastfmName, "overall", 5, 5, true, queue);
				generateImage(queue, 5, 5, e);

			} else {
				UrlCapsuleConcurrentQueue queue = new UrlCapsuleConcurrentQueue(getDao(), discogsApi, spotifyApi);
				lastFM.getUserList(lastfmName, "overall", 5, 5, false, queue);
				generateImage(queue, 5, 5, e);
			}
		} catch (LastFmEntityNotFoundException e1) {
			parser.sendError(parser.getErrorMessage(3), e);
		} catch (LastFmException ex) {
			parser.sendError(parser.getErrorMessage(2), e);
		}
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!top");
	}

	@Override
	public String getDescription() {
		return ("Your all time top albums!");
	}

	@Override
	public String getName() {
		return "Top Albums Chart";
	}
}
