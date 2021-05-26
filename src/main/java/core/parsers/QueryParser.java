package core.parsers;

import core.commands.Context;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.WordParameter;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

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
        return List.of(() -> new ExplanationLineType("search-term", "What you want to search for", OptionType.STRING));
    }

}
