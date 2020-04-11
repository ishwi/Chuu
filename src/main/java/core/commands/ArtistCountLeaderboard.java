package core.commands;

import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LbEntry;

import java.util.Collections;
import java.util.List;

public class ArtistCountLeaderboard extends LeaderboardCommand<CommandParameters> {
    public ArtistCountLeaderboard(ChuuService dao) {
        super(dao);
    }


    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getEntryName() {
        return "artist";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("scrobbledlb");
    }

    @Override
    public List<LbEntry> getList(CommandParameters params) {
        return getService().getArtistLeaderboard(params.getE().getGuild().getIdLong());

    }

    @Override
    public String getDescription() {
        return ("Users of a server ranked by number of artists scrobbled");
    }

    @Override
    public String getName() {
        return "Artist count Leaderboard";
    }

}
