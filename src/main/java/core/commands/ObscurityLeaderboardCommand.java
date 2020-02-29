package core.commands;

import dao.ChuuService;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class ObscurityLeaderboardCommand extends CrownLeaderboardCommand {
    public ObscurityLeaderboardCommand(ChuuService dao) {
        super(dao);
        this.entryName = "Obscurity points";

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
    public List<LbEntry> getList(String[] message, MessageReceivedEvent e) {
        return getService().getObscurityRankings(e.getGuild().getIdLong());
    }

    @Override
    public String getName() {
        return "Obscurity";
    }
}
