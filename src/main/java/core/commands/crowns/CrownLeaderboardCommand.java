package core.commands.crowns;

import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import core.util.ServiceView;
import dao.entities.LbEntry;

import java.util.Collections;
import java.util.List;

import static core.parsers.NumberParser.generateThresholdParser;

public class CrownLeaderboardCommand extends LeaderboardCommand<NumberParameters<CommandParameters>, Integer> {

    public CrownLeaderboardCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
    }

    @Override
    public String slashName() {
        return "artist-leaderboard";
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public String getEntryName(NumberParameters<CommandParameters> params) {
        return "Crowns";
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        return generateThresholdParser(NoOpParser.INSTANCE);
    }

    @Override
    public List<LbEntry<Integer>> getList(NumberParameters<CommandParameters> params) {
        Long threshold = params.getExtraParam();
        long idLong = params.getE().getGuild().getIdLong();

        if (threshold == null) {
            threshold = (long) db.getGuildCrownThreshold(idLong);
        }
        return db.getGuildCrownLb(params.getE().getGuild().getIdLong(), Math.toIntExact(threshold));
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
