package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class SupportCommand extends MyCommand<CommandParameters> {
    public SupportCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Link to the discord server where you can contact the bot developers";
    }

    @Override
    public List<String> getAliases() {
        return List.of("support");
    }

    @Override
    public String getName() {
        return "Support";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        sendMessageQueue(e, "For any doubt or suggestion you can contact the bot developers on:\nhttps://discord.gg/HQGqYD7");
    }
}
