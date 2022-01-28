package core.otherlisteners;

import core.Chuu;
import core.commands.abstracts.MyCommand;
import core.parsers.explanation.util.Autocompletable;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.Interactible;
import core.parsers.params.CommandParameters;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AutoCompleteListener implements EventListener {


    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof CommandAutoCompleteInteractionEvent e) {
            handle(e);
        }
    }

    private void handle(CommandAutoCompleteInteractionEvent e) {
        AutoCompleteQuery focusedOption = e.getFocusedOption();
        MyCommand<? extends CommandParameters> myCommand = Chuu.customManager.parseCommand(e);
        List<Command.Choice> interactibles = myCommand.getParser().getUsages().stream().map(Explanation::explanation)
                .filter(w -> w instanceof Autocompletable)
                .map(w -> (Autocompletable & Interactible) w)
                .filter(w -> w.options().stream().anyMatch(optionData -> optionData.isAutoComplete() && optionData.getName().equals(focusedOption.getName())))
                .flatMap(w -> w.autocomplete(e).stream()).toList();
        e.replyChoices(interactibles).queue();
    }

}
