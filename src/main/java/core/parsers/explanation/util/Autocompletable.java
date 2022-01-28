package core.parsers.explanation.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public interface Autocompletable {
    static Command.Choice of(String s) {
        if (StringUtils.isBlank(s)) {
            s = EmbedBuilder.ZERO_WIDTH_SPACE;
        }
        return new Command.Choice(StringUtils.abbreviate(s, 100), StringUtils.abbreviate(s, 100));
    }

    List<Command.Choice> autocomplete(CommandAutoCompleteInteractionEvent currentInput);
}
