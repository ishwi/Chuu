package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.CommandParameters;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

import java.util.List;

public class SubParser<T extends CommandParameters> extends Parser<T> {
    private final Parser<T> initalParser;
    private final String[] args;

    public SubParser(Parser<T> initalParser, String[] args) {
        this.initalParser = initalParser;
        this.args = args;
    }

    @Override
    public T parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        return initalParser.parseSlashLogic(ctx);
    }

    @Override
    protected T parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
        return initalParser.parseLogic(e, words);
    }

    @Override
    public String[] getSubMessage(Context context) {
        return args;
    }


    @Override
    public List<Explanation> getUsages() {
        return initalParser.getUsages();
    }
}
