package core.commands;

import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LbEntry;

import java.util.List;

public class ScrobblesLbCommand extends LeaderboardCommand<CommandParameters> {
    public ScrobblesLbCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public String getEntryName() {
        return "scrobbles";
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Users ordered by scrobbles";
    }

    @Override
    public List<String> getAliases() {
        return List.of("scrobbleslb", "slb", "scrobbledlb");
    }

    @Override
    public String getName() {
        return "Scrobble Leaderboard";
    }

    @Override
    public List<LbEntry> getList(CommandParameters params) {
        return getService().getScrobblesLeaderboard(params.getE().getGuild().getIdLong());
    }
}
