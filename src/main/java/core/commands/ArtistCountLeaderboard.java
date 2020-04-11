package core.commands;

import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LbEntry;

import java.util.Collections;
import java.util.List;

public class ArtistCountLeaderboard extends CrownLeaderboardCommand {
    public ArtistCountLeaderboard(ChuuService dao) {
        super(dao);
        this.entryName = "artist";
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
