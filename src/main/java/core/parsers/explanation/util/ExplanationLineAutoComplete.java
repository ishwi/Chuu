package core.parsers.explanation.util;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.function.Function;

public record ExplanationLineAutoComplete(String header, String usage,
                                          List<OptionData> options,
                                          Function<String, List<Command.Choice>> autocomplete) implements Interactible, Autocompletable {


    public ExplanationLineAutoComplete {
        options.forEach(optionData -> optionData.setAutoComplete(true));
    }

    public List<Command.Choice> autocomplete(String currentInput) {
        return autocomplete.apply(currentInput);
    }
}
