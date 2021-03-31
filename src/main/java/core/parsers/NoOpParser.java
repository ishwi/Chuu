package core.parsers;

import core.parsers.explanation.util.Explanation;
import core.parsers.params.CommandParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

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
    public List<Explanation> getUsages() {
        return Collections.emptyList();
    }

}
