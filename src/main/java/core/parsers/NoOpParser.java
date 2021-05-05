package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.CommandParameters;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.List;

public class NoOpParser extends Parser<CommandParameters> {

    public static final NoOpParser INSTANCE = new NoOpParser();

    public NoOpParser() {
    }

    public NoOpParser(OptionalEntity opt, OptionalEntity... opts) {
        super(ArrayUtils.insert(0, opts, opt));
    }

    @Override
    protected void setUpErrorMessages() {
        //Cleaning previous
    }

    @Override
    public CommandParameters parseSlashLogic(ContextSlashReceived e) {
        return new CommandParameters(e);
    }

    @Override

    public CommandParameters parseLogic(Context e, String[] subMessage) {
        return new CommandParameters(e);
    }

    @Override
    public List<Explanation> getUsages() {
        return Collections.emptyList();
    }


}
