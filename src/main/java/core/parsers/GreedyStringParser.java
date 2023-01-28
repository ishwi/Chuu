package core.parsers;

import core.commands.Context;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.StringParameters;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public class GreedyStringParser extends Parser<StringParameters> {

    @Override
    protected StringParameters parseLogic(Context e, String[] words) {
        return new StringParameters(e, String.join(" ", words));
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLineType("Phrase", null, OptionType.STRING));
    }

}
