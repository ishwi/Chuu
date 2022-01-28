package core.parsers.explanation.util;

import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;

public interface Autocompletable {
    List<Command.Choice> autocomplete(String currentInput);
}
