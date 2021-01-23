package core.commands.stats;

import core.apis.youtube.InvidousSearch;
import core.apis.youtube.YoutubeSearch;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.Parser;
import core.parsers.UsernameAndNpQueryParser;
import core.parsers.params.ExtraParameters;
import core.parsers.params.WordParameter;
import dao.ChuuService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

public class YoutubeSearchCommand extends ConcurrentCommand<ExtraParameters<WordParameter, User>> {
    public static boolean ONLY_YT = false;
    private final YoutubeSearch optionalSearch;

    public YoutubeSearchCommand(ChuuService dao) {
        super(dao);
        optionalSearch = new InvidousSearch();

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.NOW_PLAYING;
    }

    @Override
    public Parser<ExtraParameters<WordParameter, User>> initParser() {
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
    protected void onCommand(MessageReceivedEvent e, @NotNull ExtraParameters<WordParameter, User> params) {


        String query = params.getInnerParams().getWord();
        String s = optionalSearch.doSearch(query);
        s = s == null || s.isBlank() ? String.format("Couldn't find \"%s\" on youtube", CommandUtil.cleanMarkdownCharacter(query)) : s;
        sendMessageQueue(e, s);

    }

    @Override
    public String getName() {
        return "Youtube Search";
    }
}
