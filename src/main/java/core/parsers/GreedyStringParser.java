package core.parsers;

import core.parsers.params.StringParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GreedyStringParser extends Parser<StringParameters> {
    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected StringParameters parseLogic(MessageReceivedEvent e, String[] words) {
        return new StringParameters(e, String.join(" ", words));
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *phrase* **\n";
    }
}
