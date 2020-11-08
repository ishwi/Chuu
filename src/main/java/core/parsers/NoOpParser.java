package core.parsers;

import core.parsers.params.CommandParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NoOpParser extends Parser<CommandParameters> {


    @Override
    protected void setUpErrorMessages() {
        //Cleaning previous
    }


    @Override

    public CommandParameters parseLogic(MessageReceivedEvent e, String[] subMessage) {
        return new CommandParameters(e);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + "**\n";
    }
}
