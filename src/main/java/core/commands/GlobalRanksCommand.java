package core.commands;

import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class GlobalRanksCommand extends ConcurrentCommand<CommandParameters> {
    GlobalRanksCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return null;
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
