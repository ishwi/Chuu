package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class GlobalRanksCommand extends ConcurrentCommand {
    GlobalRanksCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public String getDescription() {
        return "Ranking on crowns, scrobbles and uniques within the bot";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("rank");
    }

    @Override
    public String getName() {
        return "Rank";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        // TODO someday
    }
}
