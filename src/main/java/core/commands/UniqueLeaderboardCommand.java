package core.commands;

import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LbEntry;

import java.util.Collections;
import java.util.List;

public class UniqueLeaderboardCommand extends LeaderboardCommand<CommandParameters> {
    public UniqueLeaderboardCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getEntryName() {
        return "Unique Artists";
    }

    @Override
    public String getDescription() {
        return ("Unique Artist leaderboard in guild ");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("uniquelb");
    }

    @Override
    public List<LbEntry> getList(CommandParameters parameters) {
        return getService().getUniqueLeaderboard(parameters.getE().getGuild().getIdLong());
    }

    @Override
    public String getName() {
        return "Unique Leaderboard";
    }

}
