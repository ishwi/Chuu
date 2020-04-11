package core.parsers;

import core.parsers.params.WordParameter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetParser extends Parser<WordParameter> {
    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(0, "You need to introduce a valid last.fm account!");
    }

    @Override
    public WordParameter parseLogic(MessageReceivedEvent e, String[] subMessage) {
        if (subMessage.length != 1) {
            sendError(getErrorMessage(0), e);
            return null;
        }
        return new WordParameter(e, subMessage[0]);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *lastFmName***\n";

    }


}
