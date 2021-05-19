package core.commands.crowns;

import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ServiceView;
import dao.entities.LbEntry;

import java.util.Arrays;
import java.util.List;

import static core.parsers.NumberParser.generateThresholdParser;

public class TrackCrownsLeaderboardCommand extends LeaderboardCommand<NumberParameters<CommandParameters>> {

    public TrackCrownsLeaderboardCommand(ServiceView dao) {
        super(dao, true);

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        return generateThresholdParser(NoOpParser.INSTANCE);
    }


    @Override
    public String getEntryName(NumberParameters<CommandParameters> params) {
        return "Track crowns leaderboard";
    }

    @Override
    public String getDescription() {
        return ("List of users ordered by number of track crowns");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("crownstracklb", "crownstracklb", "ctrlb", "crownstrlb");
    }

    @Override
    public List<LbEntry> getList(NumberParameters<CommandParameters> params) {
        Long threshold = params.getExtraParam();
        long idLong = params.getE().getGuild().getIdLong();

        if (threshold == null) {
            threshold = (long) db.getGuildCrownThreshold(idLong);
        }
        return db.trackCrownsLeaderboard(params.getE().getGuild().getIdLong(), Math.toIntExact(threshold));
    }

    @Override
    public String getName() {
        return "Track crowns leaderboard";
    }
}
