package core.commands;

import dao.ChuuService;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class AlbumCrownsLeaderboardCommand extends CrownLeaderboardCommand {
    public AlbumCrownsLeaderboardCommand(ChuuService dao) {
        super(dao);
        this.entryName = "Album Crowns";

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
    public List<LbEntry> getList(String[] message, MessageReceivedEvent e) {
        return getService().albumCrownsLeaderboard(e.getGuild().getIdLong());
    }

    @Override
    public String getName() {
        return "Album Crown Leaderboard";
    }
}
