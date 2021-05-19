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

public class AlbumCountLeaderboard extends LeaderboardCommand<NumberParameters<CommandParameters>> {
    public AlbumCountLeaderboard(ServiceView dao) {
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
        String s = "You can also introduce the playcount to only show albums above that number of plays";
        return new NumberParser<>(NoOpParser.INSTANCE,
                -0L,
                Integer.MAX_VALUE,
                map, s, false, true, true, "filter");
    }

    @Override
    public String getEntryName(NumberParameters<CommandParameters> params) {
        Long extraParam = params.getExtraParam();
        if (extraParam != 0) {
            return "albums with more than " + extraParam + " plays";
        }
        return "albums";
    }

    @Override
    public List<String> getAliases() {
        return List.of("albumslb", "alblb");
    }

    @Override
    public List<LbEntry> getList(NumberParameters<CommandParameters> params) {
        int threshold = params.getExtraParam().intValue();
        return db.getAlbumLeaderboard(params.getE().getGuild().getIdLong(), threshold == 0 ? -1 : threshold);

    }

    @Override
    public String getDescription() {
        return ("Users of a server ranked by number of albums scrobbled");
    }

    @Override
    public String getName() {
        return "Album count leaderboard";
    }

}
