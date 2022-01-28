package core.interactions;

import core.Chuu;
import core.commands.abstracts.MyCommand;
import core.commands.ui.UserCommandMarker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserCommandsBuilder {


    static CommandListUpdateAction fillAction(JDA jda, CommandListUpdateAction commandUpdateAction) {
        List<? extends MyCommand<?>> myCommands = jda.getRegisteredListeners().stream().filter(t -> t instanceof MyCommand<?> && t instanceof UserCommandMarker).map(t -> (MyCommand<?> & UserCommandMarker) t).sorted(Comparator.comparingInt(t -> t.order)).toList();
        if (myCommands.size() > 5) {
            throw new IllegalStateException("You cannot have more than 5 user commands");
        }
        List<CommandData> userCommands = myCommands.stream().map(UserCommandsBuilder::processCommand).toList();
        Map<String, MyCommand<?>> commandMap = myCommands.stream().collect(Collectors.toMap(MyCommand::slashName, mc -> mc));
        Chuu.customManager.addSlashVariants(commandMap);
        return commandUpdateAction.addCommands(userCommands);

    }

    @Nonnull
    private static CommandData processCommand(MyCommand<?> myCommand) {
        return Commands.user(WordUtils.capitalize(myCommand.slashName()));
    }
}
