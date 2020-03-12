package core.commands;

import core.apis.youtube.Search;
import core.apis.youtube.SearchSingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.UsernameAndNpQueryParser;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class YoutubeSearchCommand extends ConcurrentCommand {
    private final Search youtubeSearch;

    public YoutubeSearchCommand(ChuuService dao) {
        super(dao);
        youtubeSearch = SearchSingleton.getInstanceUsingDoubleLocking();
        this.parser = new UsernameAndNpQueryParser(dao, lastFM);
    }

    @Override
    public String getDescription() {
        return "Searches in Youtube inputted query or now playing song";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("yt", "npyt", "youtube", "you");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        String[] returned = parser.parse(e);
        if (returned == null) {
            return;
        }
        String query = returned[0];
        //long whom = Long.parseLong(returned[1]);
        String s = youtubeSearch.doSearch(query);
        s = s == null || s.isBlank() ? "Coudn't find \"" + query + "\" on youtube" : s;
        sendMessageQueue(e, s);

    }

    @Override
    public String getName() {
        return "Youtube Search";
    }
}
