package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class SourceCommand extends ConcurrentCommand<CommandParameters> {
    private static final String REPO_URL = "https://github.com/ishwi/discordBot";


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.BOT_INFO;
    }

    public SourceCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Source code of the bot";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("source");
    }

    @Override
    public String getName() {
        return "Source";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        sendMessageQueue(e, String.format("This is the GitHub link of the bot:%n%s", REPO_URL));
    }
}
