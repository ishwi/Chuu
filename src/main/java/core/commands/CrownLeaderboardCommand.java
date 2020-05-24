package core.commands;

import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.LbEntry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class CrownLeaderboardCommand extends LeaderboardCommand<NumberParameters<CommandParameters>> {

    public CrownLeaderboardCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;

    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public String getEntryName(NumberParameters<CommandParameters> params) {
        return "Crowns";
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> getParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to vary the number of plays to award a crown, " +
                "defaults to whatever the guild has configured (0 if not configured)";
        return new NumberParser<>(new NoOpParser(),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true);
    }

    @Override
    public List<LbEntry> getList(NumberParameters<CommandParameters> params) {
        Long threshold = params.getExtraParam();
        long idLong = params.getE().getGuild().getIdLong();

        if (threshold == null) {
            threshold = (long) getService().getGuildCrownThreshold(idLong);
        }
        return getService().getGuildCrownLb(params.getE().getGuild().getIdLong(), Math.toIntExact(threshold));
    }


    @Override
    public String getDescription() {
        return ("Users of a server ranked by crowns");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("crownslb");
    }

    @Override
    public String getName() {
        return "Crown Leaderboard";
    }


}
