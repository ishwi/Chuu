package core.commands;

import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LbEntry;

import java.util.Arrays;
import java.util.List;

public class ObscurityLeaderboardCommand extends LeaderboardCommand<CommandParameters> {
    public ObscurityLeaderboardCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getEntryName() {
        return "Obscurity points";
    }

    @Override
    public String getDescription() {
        return "Gets how \\*obscure\\* your scrobbled artist are in relation with all the rest of the users of the server";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("obscuritylb", "ob", "obs");
    }

    @Override
    public List<LbEntry> getList(CommandParameters params) {
        return getService().getObscurityRankings(params.getE().getGuild().getIdLong());

    }


    @Override
    public String getName() {
        return "Obscurity";
    }
}
