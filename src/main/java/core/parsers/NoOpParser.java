package core.parsers;

import core.parsers.params.CommandParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NoOpParser extends Parser<CommandParameters> {


    @Override
    protected void setUpErrorMessages() {
        //Cleaning previous
    }

    @Override
    public CommandParameters parse(MessageReceivedEvent e) {
        throw new UnsupportedOperationException();
    }

    @Override

    public CommandParameters parseLogic(MessageReceivedEvent e, String[] subMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + "**\n\n";
    }
}
