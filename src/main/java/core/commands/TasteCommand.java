package core.commands;

import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.TwoUsersParser;
import core.parsers.params.TwoUsersParamaters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ResultWrapper;
import dao.entities.UserArtistComparison;
import dao.entities.UserInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TasteCommand extends BaseTasteCommand<TwoUsersParamaters> {
    public TasteCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public String getEntity(TwoUsersParamaters params) {
        return "artists";
    }

    @Override
    public String hasCustomUrl(TwoUsersParamaters params) {
        return null;
    }

    @Override
    public Parser<TwoUsersParamaters> initParser() {
        TwoUsersParser twoUsersParser = new TwoUsersParser(getService(), new OptionalEntity("list", "display in a list format"));
        twoUsersParser.setExpensiveSearch(true);
        return twoUsersParser;
    }

    @Override
    public String getDescription() {
        return "Compare Your musical taste with another user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("taste", "t", "compare");
    }


    @Override
    public Pair<LastFMData, LastFMData> getUserDatas(MessageReceivedEvent e, TwoUsersParamaters params) {
        return Pair.of(params.getFirstUser(), params.getSecondUser());
    }

    @Override
    public ResultWrapper<UserArtistComparison> getResult(LastFMData og, LastFMData second, TwoUsersParamaters params) {
        boolean isList = params.hasOptional("list");
        return getService().getSimilarities(List.of(og.getName(), second.getName()), isList ? 200 : 10);
    }

    @Override
    public Pair<Integer, Integer> getTasteBar(ResultWrapper<UserArtistComparison> resultWrapper, UserInfo og, UserInfo second, TwoUsersParamaters params) {
        return Pair.of(og.getPlayCount(), second.getPlayCount());
    }


    @Override
    public String getName() {
        return "Taste";
    }

}
