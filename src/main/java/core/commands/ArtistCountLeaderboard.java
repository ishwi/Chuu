package core.commands;

import dao.ChuuService;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    public List<LbEntry> getList(String[] message, MessageReceivedEvent e) {
        return getService().getArtistLeaderboard(e.getGuild().getIdLong());
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
