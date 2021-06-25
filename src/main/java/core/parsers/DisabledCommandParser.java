package core.parsers;

import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.ContextSlashReceived;
import core.commands.abstracts.MyCommand;
import core.commands.moderation.DisabledCommand;
import core.exceptions.LastFmException;
import core.interactions.InteractionBuilder;
import core.parsers.explanation.CommandExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.DisabledCommandParameters;
import core.parsers.utils.OptionalEntity;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DisabledCommandParser extends Parser<DisabledCommandParameters> implements Generable<DisabledCommandParameters> {

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    void setUpOptionals() {
        addOptional(
                new OptionalEntity("channel", "only does the toggle in this specific channel instead of in the whole server"),
                new OptionalEntity("category", "disable all commands from the category of the command introduced"),
                new OptionalEntity("all", "disable all commands from all categories of the command introduced"),
                new OptionalEntity("exceptthis", "disable all commands from all categories ofs the command introduced"));


    }

    @Override
    public DisabledCommandParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        return parseLogic(ctx, new String[]{ctx.e().getOption("command-name").getAsString()});
    }

    @Override
    protected DisabledCommandParameters parseLogic(Context e, String[] words) {
        DisabledCommandParameters.Action action;
        if (e instanceof ContextMessageReceived cmr) {
            String contentRaw = cmr.e().getMessage().getContentRaw();
            action = switch (contentRaw.toLowerCase().charAt(1)) {
                case 't' -> DisabledCommandParameters.Action.TOGGLE;
                case 'd' -> DisabledCommandParameters.Action.DISABLE;
                default -> DisabledCommandParameters.Action.ENABLE;
            };
        } else {
            var csr = ((ContextSlashReceived) e);
            action = switch (csr.e().getSubcommandName().charAt(0)) {
                case 't' -> DisabledCommandParameters.Action.TOGGLE;
                case 'd' -> DisabledCommandParameters.Action.DISABLE;
                default -> DisabledCommandParameters.Action.ENABLE;
            };
        }
        List<Character> acceptecChars = PrefixParser.acceptecChars;
        if (e.getMember() == null || !e.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            sendError("Only server admins can disable commands", e);
            return null;
        }
        if (words.length != 1) {
            if (hasOptional("all", e)) {
                return new DisabledCommandParameters(e, null, e.getGuild().getIdLong(), e.getChannel().getIdLong(), action);
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
        List<? extends MyCommand<?>> commands = e.getJDA().getRegisteredListeners()
                .stream().filter(x -> x instanceof MyCommand).map(x -> ((MyCommand<?>) x)).toList();

        Optional<? extends MyCommand<?>> first = commands.stream().filter(x ->
                x.getAliases().stream().anyMatch(y -> y.equalsIgnoreCase(finalWord))).findFirst();
        if (first.isEmpty()) {
            sendError("Couldn't find any command called " + word, e);
            return null;
        }
        if (first.get() instanceof DisabledCommand) {
            sendError("You can't disable the disable command!", e);
            return null;
        }

        return new DisabledCommandParameters(e, first.get(), e.getGuild().getIdLong(), e.getChannel().getIdLong(), action);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLineType("command-name", "The name of a command", OptionType.STRING),
                new CommandExplanation("This command has different alias (enable,disable,toggle), and depending on the alias used the result will be different"));
    }


    @Override
    public CommandData generateCommandData(MyCommand<?> myCommand) {
        CommandData commands = new CommandData(myCommand.slashName(), myCommand.getDescription());
        var a = new SubcommandData("enable", "Enables the command");
        var b = new SubcommandData("disable", "Disables the command");
        var c = new SubcommandData("toggle", "Toggles the command");
        List<OptionData> optionData = new ArrayList<>();
        InteractionBuilder.processOpts(myCommand, optionData::add);
        a.addOption(OptionType.STRING, "command-name", "The command to apply the action to");
        b.addOption(OptionType.STRING, "command-name", "The command to apply the action to");
        c.addOption(OptionType.STRING, "command-name", "The command to apply the action to");
        a.addOptions(optionData);
        b.addOptions(optionData);
        c.addOptions(optionData);
        commands.addSubcommands(List.of(a, b, c));
        return commands;
    }
}
