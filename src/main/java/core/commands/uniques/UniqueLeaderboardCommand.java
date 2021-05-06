package core.commands.uniques;

import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
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
    protected CommandCategory initCategory() {
        return CommandCategory.UNIQUES;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getEntryName(CommandParameters params) {
        return "unique artists";
    }

    @Override
    public String getDescription() {
        return ("Unique artist leaderboard in guild");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("uniquelb");
    }

    @Override
    public List<LbEntry> getList(CommandParameters parameters) {
        return db.getUniqueLeaderboard(parameters.getE().getGuild().getIdLong());
    }

    @Override
    public String getName() {
        return "Unique Leaderboard";
    }

}
