package core.commands.leaderboards;

import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ServiceView;
import dao.entities.LbEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class TrackCountLeaderboard extends LeaderboardCommand<NumberParameters<CommandParameters>, Integer> {
    public TrackCountLeaderboard(ServiceView dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_LEADERBOARDS;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce the playcount to only show songs above that number of plays";
        return new NumberParser<>(NoOpParser.INSTANCE,
                -0L,
                Integer.MAX_VALUE,
                map, s, false, true, true, "filter");
    }

    @Override
    public String slashName() {
        return "tracks";
    }

    @Override
    public String getEntryName(NumberParameters<CommandParameters> params) {
        Long extraParam = params.getExtraParam();
        if (extraParam != 0) {
            return "songs with more than " + extraParam + " plays";
        }
        return "songs";
    }

    @Override
    public List<String> getAliases() {
        return List.of("songslb", "tralb");
    }

    @Override
    public List<LbEntry<Integer>> getList(NumberParameters<CommandParameters> params) {
        int threshold = params.getExtraParam().intValue();
        return db.getTrackLeaderboard(params.getE().getGuild().getIdLong(), threshold == 0 ? -1 : threshold);

    }

    @Override
    public String getDescription() {
        return ("Users of a server ranked by number of songs scrobbled");
    }

    @Override
    public String getName() {
        return "Song count leaderboard";
    }

}
