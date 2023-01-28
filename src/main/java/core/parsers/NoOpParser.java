package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.CommandParameters;
import core.parsers.utils.OptionalEntity;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
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
    public CommandParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) {
        return new CommandParameters(ctx);
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
