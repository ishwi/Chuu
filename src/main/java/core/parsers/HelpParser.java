package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.WordParameter;
import core.parsers.utils.OptionalEntity;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.Optional;

public class HelpParser extends Parser<WordParameter> {
    public HelpParser(OptionalEntity... opt) {
        super(opt);
    }

    @Override
    public WordParameter parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) {
        return new WordParameter(ctx, Optional.ofNullable(ctx.e().getOption("command")).map(OptionMapping::getAsString).orElse(null));
    }

    @Override
    protected WordParameter parseLogic(Context e, String[] words) {
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
