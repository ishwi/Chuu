package core.commands.leaderboards;

import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.LbEntry;

import java.util.List;

public class ScrobblesLbCommand extends LeaderboardCommand<CommandParameters> {
    public ScrobblesLbCommand(ServiceView dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_LEADERBOARDS;
    }

    @Override
    public String getEntryName(CommandParameters params) {
        return "scrobbles";
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
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
        return db.getScrobblesLeaderboard(params.getE().getGuild().getIdLong());
    }
}
