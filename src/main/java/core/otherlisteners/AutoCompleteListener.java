package core.otherlisteners;

import core.Chuu;
import core.commands.abstracts.MyCommand;
import core.parsers.explanation.util.Autocompletable;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.CommandParameters;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class AutoCompleteListener implements EventListener {


    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof CommandAutoCompleteInteractionEvent e) {
            handle(e);
        }
    }

    private void handle(CommandAutoCompleteInteractionEvent e) {
        AutoCompleteQuery focusedOption = e.getFocusedOption();
        String commandPath = e.getCommandPath();
        Map<String, MyCommand<? extends CommandParameters>> slashVariants = Chuu.customManager.getSlashVariants();
        MyCommand<? extends CommandParameters> myCommand = slashVariants.get(commandPath);
        List<Command.Choice> interactibles = myCommand.getParser().getUsages().stream().map(Explanation::explanation).filter(w -> w instanceof Autocompletable)
                .flatMap(w -> ((Autocompletable) w).autocomplete(focusedOption.getValue()).stream()).toList();
        if (!interactibles.isEmpty()) {
            e.replyChoices(interactibles).queue();
        }
    }
}
