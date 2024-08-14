package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.StringParameters;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public class GreedyStringParser extends Parser<StringParameters> {

    private final String name;

    public GreedyStringParser(String name) {
        this.name = name;
    }

    public GreedyStringParser() {
        this("text");
    }

    @Override
    protected StringParameters parseLogic(Context e, String[] words) {
        return new StringParameters(e, String.join(" ", words));
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLineType(name, "each spaces a new word", OptionType.STRING));
    }

    @Override
    public StringParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        String phrase = ctx.e().getOption(name, OptionMapping::getAsString);
        return new StringParameters(ctx, phrase);
    }
}
