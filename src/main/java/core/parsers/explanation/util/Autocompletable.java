package core.parsers.explanation.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@FunctionalInterface
public interface Autocompletable {

    Command.Choice EMPTY_CHOICE = new Command.Choice(EmbedBuilder.ZERO_WIDTH_SPACE, EmbedBuilder.ZERO_WIDTH_SPACE);

    static Command.Choice of(String s) {
        if (StringUtils.isBlank(s)) {
            return EMPTY_CHOICE;
        }
        return new Command.Choice(StringUtils.abbreviate(s, 100), StringUtils.abbreviate(s, 100));
    }

    List<Command.Choice> autocomplete(CommandAutoCompleteInteractionEvent currentInput);
}
