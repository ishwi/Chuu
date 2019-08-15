package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.RandomUrlEntity;
import main.Parsers.RandomAlbumParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class RandomAlbumCommand extends ConcurrentCommand {
	public RandomAlbumCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new RandomAlbumParser();
	}

	@Override
	protected void threadableCode(MessageReceivedEvent e) {
		String[] returned;

		returned = parser.parse(e);
		if (returned == null)
			return;
		if (returned.length == 0) {
			//get randomurl
			RandomUrlEntity randomUrl = getDao().getRandomUrl();
			if (randomUrl == null) {
				sendMessage(e, "The pool of urls was empty, add one first! ");
				return;
			}
			String sb = e.getAuthor().getAsMention() + ", here's your random recommendation\n" +
					"**Posted by:** " +
					getUserGlobalString(randomUrl.getDiscordId(), e, "unknown") + "\n**Link:** " +
					randomUrl.getUrl();
			sendMessage(e, sb);

			return;
		}
		//add url
		if (!getDao().addToRandomPool(new RandomUrlEntity(returned[0], e.getAuthor().getIdLong(), e.getGuild()
				.getIdLong()))) {
			sendMessage(e, "The provied url: " + returned[0] + " was already on the pool");
			return;
		}
		sendMessage(e, "Succesfully added " + getUserString(e.getAuthor().getIdLong(), e, e.getAuthor()
				.getName()) + "'s link  to the pool");

	}

	@Override
	String getDescription() {
		return "Gets a random url that other users have added , or add one yourself";
	}

	@Override
	String getName() {
		return "Random Url";
	}

	@Override
	List<String> getAliases() {
		return Collections.singletonList("!random");
	}
}
