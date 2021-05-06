package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.WordParameter;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.Optional;

public class HelpParser extends Parser<WordParameter> {
    public HelpParser(OptionalEntity opt) {
        super(opt);
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    public WordParameter parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        return new WordParameter(ctx, Optional.ofNullable(ctx.e().getOption("command")).map(OptionMapping::getAsString).orElse(null));
    }

    @Override
    protected WordParameter parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException {
        if (words.length == 0) {
            return new WordParameter(e, null);
        }
        return new WordParameter(e, words[0]);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLineType("command", "name of the command to search", OptionType.STRING));
    }
}
