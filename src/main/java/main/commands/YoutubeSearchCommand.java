package main.commands;

import dao.DaoImplementation;
import main.apis.Youtube.Search;
import main.apis.Youtube.SearchSingleton;
import main.parsers.UsernameAndNpQueryParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class YoutubeSearchCommand extends ConcurrentCommand {
	private final Search youtubeSearch;

	public YoutubeSearchCommand(DaoImplementation dao) {
		super(dao);
		youtubeSearch = SearchSingleton.getInstanceUsingDoubleLocking();
		this.parser = new UsernameAndNpQueryParser(dao, lastFM);
	}

	@Override
	String getDescription() {
		return "Searches in Youtube inputted query or now playing song";
	}

	@Override
	List<String> getAliases() {
		return Collections.singletonList("yt");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) {

		String[] returned = parser.parse(e);
		if (returned == null) {
			return;
		}
		String query = returned[0];
		//long whom = Long.parseLong(returned[1]);
		sendMessageQueue(e, youtubeSearch.doSearch(query));

	}

	@Override
	String getName() {
		return "Youtube Search";
	}
}
