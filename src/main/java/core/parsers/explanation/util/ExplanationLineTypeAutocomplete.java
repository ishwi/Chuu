package core.parsers.explanation.util;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public record ExplanationLineTypeAutocomplete(String header, String usage, OptionType type,
                                              Autocompletable autocomplete) implements Interactible, Autocompletable {

    @Override
    public List<OptionData> options() {
        return List.of(new OptionData(type, header.replaceAll("\\s", "-").replaceAll("\\.", "-"), StringUtils.abbreviate(usage, 100)).setAutoComplete(true));
    }

    public List<Command.Choice> autocomplete(CommandAutoCompleteInteractionEvent currentInput) {
        return autocomplete.autocomplete(currentInput);
    }

}
