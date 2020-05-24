package core.commands;

import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LbEntry;

import java.util.Arrays;
import java.util.List;

public class AlbumCrownsLeaderboardCommand extends LeaderboardCommand<CommandParameters> {

    public AlbumCrownsLeaderboardCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getEntryName(CommandParameters params) {
        return "Album Crowns";
    }

    @Override
    public String getDescription() {
        return ("List of users ordered by number of crown albums");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("crownsalbumlb", "crownsallb");
    }

    @Override
    public List<LbEntry> getList(CommandParameters params) {
        return getService().albumCrownsLeaderboard(params.getE().getGuild().getIdLong());
    }

    @Override
    public String getName() {
        return "Album Crown Leaderboard";
    }
}
