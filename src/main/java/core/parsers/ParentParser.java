package core.parsers;

import core.commands.InteracionReceived;
import core.commands.abstracts.MyCommand;
import core.exceptions.LastFmException;
import core.interactions.InteractionBuilder;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.EnumParameters;
import core.util.Deps;
import core.util.Descriptible;
import core.util.Subcommand;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.*;

public class ParentParser<T extends Enum<T> & Subcommand & Descriptible> extends EnumParser<T> implements Generable<EnumParameters<T>> {
    private final Deps deps;

    public ParentParser(Class<T> tClass, Deps deps) {
        super(tClass);
        this.deps = deps;
    }

    public ParentParser(Class<T> tClass, Deps deps, boolean allowEmpty, boolean hasParams, boolean continueOnNomatch) {
        super(tClass, allowEmpty, hasParams, continueOnNomatch);
        this.deps = deps;

    }

    @Override
    public EnumParameters<T> parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        String subcommandName = ctx.e().getSubcommandName();
        assert subcommandName != null;
        Optional<T> first = EnumSet.allOf(clazz).stream().filter(z -> subcommandName.equalsIgnoreCase(z.name())).findFirst();
        assert first.isPresent();
        T t = first.get();
        return new EnumParameters<>(ctx, t);
    }

    @Override
    public List<Explanation> getUsages() {

        List<String> lines = EnumSet.allOf(clazz).stream().map(x -> x.name().replaceAll("_", "-").toLowerCase()).toList();
        OptionData data = new OptionData(OptionType.STRING, "subcommands", "One of the possible subcommands to execute");

        for (String line : lines) {
            data.addChoice(line, line);
        }

        Explanation option = () -> new ExplanationLine("option", "One of the possible subcommands to execute: **" + String.join("**, **", lines) + "**", data);
        return List.of(option);

    }

    @Override
    public SlashCommandData generateCommandData(MyCommand<?> myCommand) {
        SlashCommandData commandData = Commands.slash(myCommand.slashName(), myCommand.getDescription());
        for (T t : EnumSet.allOf(clazz)) {
            SubcommandData sub = new SubcommandData(t.name().toLowerCase(Locale.ROOT), t.getDescription());
            Parser<?> parser = t.getSubcommandEx().getParser(deps);
            List<Explanation> usages = parser.getUsages();
            usages.forEach(usage -> sub.addOptions(new ArrayList<>(usage.explanation().options())));
            InteractionBuilder.processOpts(parser.getOptionals(), sub::addOptions);
            commandData.addSubcommands(sub);
        }
        return commandData;

    }
}
