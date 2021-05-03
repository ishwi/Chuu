package core.parsers;

import core.parsers.explanation.util.Explanation;
import core.parsers.params.CommandParameters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.List;

public class NoOpParser extends Parser<CommandParameters> {

    public static final NoOpParser INSTANCE = new NoOpParser();

    private NoOpParser() {
    }

    public NoOpParser(OptionalEntity opt, OptionalEntity... opts) {
        super(ArrayUtils.insert(0, opts, opt));
    }

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
