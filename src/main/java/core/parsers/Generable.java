package core.parsers;

import core.commands.abstracts.MyCommand;
import core.parsers.params.CommandParameters;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface Generable<T extends CommandParameters> {

    CommandData generateCommandData(MyCommand<?> myCommand);
}
