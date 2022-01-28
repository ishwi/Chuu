package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.WordParameter;
import core.parsers.utils.OptionalEntity;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.Optional;

public class QueryParser extends Parser<WordParameter> {
    private final boolean allowEmpty;

    public QueryParser(boolean allowEmpty, OptionalEntity... opts) {
        super(opts);
        this.allowEmpty = allowEmpty;
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    public WordParameter parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        String word = Optional.ofNullable(ctx.e().getOption("search-term")).map(OptionMapping::getAsString).orElse(null);
        if (word == null && !allowEmpty) {
            sendError("Need at least one word!", ctx);
            return null;
        }
        return new WordParameter(ctx, word);
    }

    @Override
    protected WordParameter parseLogic(Context e, String[] words) {
        String join = String.join(" ", words);
        if (!allowEmpty && join.isBlank()) {
            sendError("Need at least one word!", e);
            return null;
        }
        return new WordParameter(e, join);
    }

    @Override
    public List<Explanation> getUsages() {
        if (allowEmpty) {
            return List.of(() -> (new ExplanationLineType("search-term", "What you want to search for", OptionType.STRING)));
        }
        return List.of(InteractionAux.required(() -> (new ExplanationLineType("search-term", "What you want to search for", OptionType.STRING))));
    }

}
