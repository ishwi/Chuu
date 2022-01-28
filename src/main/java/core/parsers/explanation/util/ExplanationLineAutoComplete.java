package core.parsers.explanation.util;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.function.Function;

public record ExplanationLineAutoComplete(String header, String usage,
                                          List<OptionData> options,
                                          Function<CommandAutoCompleteInteractionEvent, List<Command.Choice>> autocomplete) implements Interactible, Autocompletable {


    public ExplanationLineAutoComplete {
        options.forEach(optionData -> optionData.setAutoComplete(true));
    }

    public List<Command.Choice> autocomplete(CommandAutoCompleteInteractionEvent currentInput) {
        return autocomplete.apply(currentInput);
    }
}
