package core.commands;

import core.apis.youtube.Search;
import core.apis.youtube.SearchSingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.UsernameAndNpQueryParser;
import core.parsers.params.ExtraParameters;
import core.parsers.params.WordParameter;
import dao.ChuuService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class YoutubeSearchCommand extends ConcurrentCommand<ExtraParameters<WordParameter, User>> {
    private final Search youtubeSearch;

    public YoutubeSearchCommand(ChuuService dao) {
        super(dao);
        youtubeSearch = SearchSingleton.getInstance();
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<ExtraParameters<WordParameter, User>> getParser() {
        return new UsernameAndNpQueryParser(getService(), lastFM);
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

        ExtraParameters<WordParameter, User> returned = parser.parse(e);
        if (returned == null) {
            return;
        }
        String query = returned.getInnerParams().getWord();
        String s = youtubeSearch.doSearch(query);
        s = s == null || s.isBlank() ? String.format("Couldn't find \"%s\" on youtube", CommandUtil.cleanMarkdownCharacter(query)) : s;
        sendMessageQueue(e, s);

    }

    @Override
    public String getName() {
        return "Youtube Search";
    }
}
