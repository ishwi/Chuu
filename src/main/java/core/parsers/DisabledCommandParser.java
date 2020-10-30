package core.parsers;

import core.commands.DisabledCommand;
import core.commands.MyCommand;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.DisabledCommandParameters;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DisabledCommandParser extends Parser<DisabledCommandParameters> {

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("channel", "only does the toggle in this specific channel instead of in the whole server"));
        opts.add(new OptionalEntity("category", "disable all commands from the category of the command introduced"));
        opts.add(new OptionalEntity("all", "disable all commands from all categories of the command introduced"));
        opts.add(new OptionalEntity("exceptthis", "disable all commands from all categories ofs the command introduced"));


    }

    @Override
    protected DisabledCommandParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        List<Character> acceptecChars = PrefixParser.acceptecChars;
        if (e.getMember() == null || !e.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            sendError("Only server admins can disable commands", e);
            return null;
        }
        if (words.length != 1) {
            if (hasOptional("all", e)) {
                return new DisabledCommandParameters(e, null, e.getGuild().getIdLong(), e.getChannel().getIdLong());
            } else {
                sendError("Only introduce the alias of the command to be disabled", e);
                return null;
            }
        }
        String word = words[0];
        if (acceptecChars.contains(word.charAt(0))) {
            word = word.substring(1);
        }
        String finalWord = word;
        List<? extends MyCommand<?>> collect = e.getJDA().getRegisteredListeners()
                .stream().filter(x -> x instanceof MyCommand).map(x -> ((MyCommand<?>) x)).collect(Collectors.toList());

        Optional<? extends MyCommand<?>> first = collect.stream().filter(x ->
                x.getAliases().stream().anyMatch(y -> y.equalsIgnoreCase(finalWord))).findFirst();
        if (first.isEmpty()) {
            sendError("Couldn't find any command called " + word, e);
            return null;
        }
        if (first.get() instanceof DisabledCommand) {
            sendError("You can't disable the disable command!", e);
            return null;
        }
        return new DisabledCommandParameters(e, first.get(), e.getGuild().getIdLong(), e.getChannel().getIdLong());
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " Alias of a Command\n" +
                "\tThis command has different alias (enable,disable,toggle), and depending on the alias used the result will be different\n ";

    }
}
