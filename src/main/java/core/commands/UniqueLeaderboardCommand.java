package core.commands;

import dao.ChuuService;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class UniqueLeaderboardCommand extends CrownLeaderboardCommand {
    public UniqueLeaderboardCommand(ChuuService dao) {
        super(dao);
        this.entryName = "Unique Artists";
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
    public List<LbEntry> getList(String[] message, MessageReceivedEvent e) {
        return getService().getUniqueLeaderboard(e.getGuild().getIdLong());
    }

    @Override
    public String getName() {
        return "Unique Leaderboard";
    }

}
