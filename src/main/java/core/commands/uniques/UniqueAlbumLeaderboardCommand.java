package core.commands.uniques;

import core.commands.abstracts.LeaderboardCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.LbEntry;

import java.util.List;

public class UniqueAlbumLeaderboardCommand extends LeaderboardCommand<CommandParameters, Integer> {
    public UniqueAlbumLeaderboardCommand(ServiceView dao) {
        super(dao, true);

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
        return "unique albums";
    }

    @Override
    public String getDescription() {
        return ("Unique album leaderboard in guild");
    }

    @Override
    public List<String> getAliases() {
        return List.of("uniquealbumlb", "uniquealblb");
    }

    @Override
    public List<LbEntry<Integer>> getList(CommandParameters parameters) {
        return db.getUniqueAlbumLeaderboard(parameters.getE().getGuild().getIdLong());
    }

    @Override
    public String getName() {
        return "Unique albums leaderboard";
    }

}
