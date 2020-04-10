package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NoOpParser extends Parser {


    @Override
    protected void setUpErrorMessages() {
        //Cleaning previous
    }

    @Override
    public String[] parse(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        return new String[]{};
    }

    @Override

    public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + "**\n\n";
    }
}
