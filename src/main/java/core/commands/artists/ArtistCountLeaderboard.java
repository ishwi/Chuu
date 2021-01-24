package core.commands.artists;

import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.LbEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class ArtistCountLeaderboard extends LeaderboardCommand<NumberParameters<CommandParameters>> {
    public ArtistCountLeaderboard(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce the playcount to only show artists above that number of plays";
        return new NumberParser<>(new NoOpParser(),
                -0L,
                Integer.MAX_VALUE,
                map, s, false, true, true);
    }

    @Override
    public String getEntryName(NumberParameters<CommandParameters> params) {
        Long extraParam = params.getExtraParam();
        if (extraParam != 0) {
            return "artist with more than " + extraParam + " plays";
        }
        return "artist";
    }

    @Override
    public List<String> getAliases() {
        return List.of("artistslb", "alb");
    }

    @Override
    public List<LbEntry> getList(NumberParameters<CommandParameters> params) {
        int threshold = params.getExtraParam().intValue();
        return db.getArtistLeaderboard(params.getE().getGuild().getIdLong(), threshold == 0 ? -1 : threshold);

    }

    @Override
    public String getDescription() {
        return ("Users of a server ranked by number of artists scrobbled");
    }

    @Override
    public String getName() {
        return "Artist count Leaderboard";
    }

}
