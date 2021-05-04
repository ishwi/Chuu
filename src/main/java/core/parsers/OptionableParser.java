package core.parsers;

import core.commands.Context;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.CommandParameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OptionableParser extends Parser<CommandParameters> {
    public OptionableParser(OptionalEntity... strings) {
        super();
        opts.addAll(Arrays.asList(strings));
    }

    @Override
    protected void setUpErrorMessages() {
        //Overriding default
    }

    @Override
    public CommandParameters parseLogic(Context e, String[] words) {
        return new CommandParameters(e);
    }

    @Override
    public List<Explanation> getUsages() {
        return Collections.emptyList();
    }

}
