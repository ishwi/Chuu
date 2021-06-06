package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.CommandExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.LOONAParameters;
import dao.ChuuService;
import dao.entities.LOONA;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import javacutils.Pair;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LOOONAParser extends DaoParser<LOONAParameters> {
    public LOOONAParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
    }

    @Override
    void setUpOptionals() {
        // Intentionally Empty
    }

    @Override
    protected void setUpErrorMessages() {
        // Intentionally Empty
    }

    @Override
    public LOONAParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User user = InteractionAux.parseUser(e);
        LastFMData data = findLastfmFromID(user, ctx);


        LOONA.Type type = LOONA.Type.valueOf(e.getOption("group-selector").getAsString());
        LOONAParameters.Display display = LOONAParameters.Display.valueOf(e.getOption("operations").getAsString());

        LOONAParameters.Mode modes = LOONAParameters.Mode.valueOf(e.getOption("modes").getAsString());
        LOONAParameters.Subject target = LOONAParameters.Subject.valueOf(e.getOption("target").getAsString());
        return new LOONAParameters(ctx, data, LOONAParameters.SubCommand.GENERAL, display, null, type, target, modes);
    }

    @Override
    protected LOONAParameters parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        EnumSet<LOONA> loonas = EnumSet.allOf(LOONA.class);
        Map<Predicate<String>, LOONA> predicateToLOONA = loonas.stream().collect(Collectors.toMap(LOONA::getParser, x -> x));
        LOONAParameters.Display display = LOONAParameters.Display.COLLAGE;
        LOONAParameters.SubCommand subCommand = null;
        LOONAParameters.Subject subject = LOONAParameters.Subject.SERVER;
        LOONA targetedLOONA = null;
        LOONA.Type targetedType = null;
        LOONAParameters.Mode mode = LOONAParameters.Mode.UNGROUPED;


        for (Predicate<String> stringPredicate : predicateToLOONA.keySet()) {
            Pair<String[], LOONA> stringPair = filterMessage(words, stringPredicate, x -> predicateToLOONA.get(stringPredicate), null);
            if (stringPair.second != null) {
                subCommand = LOONAParameters.SubCommand.SPECIFIC;
                targetedLOONA = stringPair.second;
                words = stringPair.first;
                break;
            }
        }
        if (subCommand == null) {
            EnumSet<LOONA.Type> types = EnumSet.allOf(LOONA.Type.class);
            Map<Predicate<String>, LOONA.Type> typeMap = types.stream().collect(Collectors.toMap(LOONA::getTypeParser, x -> x));
            for (Predicate<String> stringPredicate : typeMap.keySet()) {
                Pair<String[], LOONA.Type> stringPair = filterMessage(words, stringPredicate, x -> typeMap.get(stringPredicate), null);
                if (stringPair.second != null) {
                    subCommand = LOONAParameters.SubCommand.GROUPED;
                    targetedType = stringPair.second;
                    words = stringPair.first;
                    break;
                }
            }
        }
        if (subCommand == null) {
            subCommand = LOONAParameters.SubCommand.GENERAL;
        }
        EnumSet<LOONAParameters.Display> types = EnumSet.allOf(LOONAParameters.Display.class);
        Map<Predicate<String>, LOONAParameters.Display> displayMap = types.stream().collect(Collectors.toMap(x -> (String s) -> s.equalsIgnoreCase(x.toString()), x -> x));
        for (Predicate<String> stringPredicate : displayMap.keySet()) {
            Pair<String[], LOONAParameters.Display> stringPair = filterMessage(words, stringPredicate, x -> displayMap.get(stringPredicate), null);
            if (stringPair.second != null) {
                display = stringPair.second;
                words = stringPair.first;
                break;
            }
        }
        EnumSet<LOONAParameters.Subject> subjects = EnumSet.allOf(LOONAParameters.Subject.class);
        Map<Predicate<String>, LOONAParameters.Subject> subJectMap = subjects.stream().collect(Collectors.toMap(x -> (String s) -> s.equalsIgnoreCase(x.toString()), x -> x));
        for (Predicate<String> stringPredicate : subJectMap.keySet()) {
            Pair<String[], LOONAParameters.Subject> stringPair = filterMessage(words, stringPredicate, x -> subJectMap.get(stringPredicate), null);
            if (stringPair.second != null) {
                subject = stringPair.second;
                words = stringPair.first;
                break;
            }
        }
        EnumSet<LOONAParameters.Mode> modes = EnumSet.allOf(LOONAParameters.Mode.class);
        Map<Predicate<String>, LOONAParameters.Mode> modeMap = modes.stream().collect(Collectors.toMap(x -> (String s) -> s.equalsIgnoreCase(x.toString()), x -> x));
        for (Predicate<String> stringPredicate : modeMap.keySet()) {
            Pair<String[], LOONAParameters.Mode> stringPair = filterMessage(words, stringPredicate, x -> modeMap.get(stringPredicate), null);
            if (stringPair.second != null) {
                mode = stringPair.second;

                words = stringPair.first;
                break;
            }
        }


        LastFMData lastFMData = atTheEndOneUser(e, words);

        return new LOONAParameters(e, lastFMData, subCommand, display, targetedLOONA, targetedType, subject, mode);
    }

    @Override
    public List<Explanation> getUsages() {
        String types = Arrays.stream(LOONA.Type.values()).map(Enum::toString).collect(Collectors.joining("|"));
        String loonas = Arrays.stream(LOONA.values()).map(Enum::toString).map(x -> x.replaceAll("_", " ")).collect(Collectors.joining("|"));
        String modes = Arrays.stream(LOONAParameters.Mode.values()).map(Enum::toString).collect(Collectors.joining("|"));
        String subject = Arrays.stream(LOONAParameters.Subject.values()).map(Enum::toString).collect(Collectors.joining("|"));
        String operations = Arrays.stream(LOONAParameters.Display.values()).map(Enum::toString).collect(Collectors.joining("|"));


        String selector = String.format(("[%s|%s]"), types, loonas);
        String ops = String.format(("[%s]"), operations);
        String mods = String.format(("[%s]"), modes);
        String target = String.format(("[%s]"), subject);


        OptionData selectorData = new OptionData(OptionType.STRING, "group-selector", StringUtils.abbreviate("You can either select all the members, a specific member,all subgroups, a specific subgroup,the main group or what is tagged as Misc.", 100), true);
        for (LOONA.Type value : LOONA.Type.values()) {
            String s = value.toString().replaceAll("_", " ");
            selectorData.addChoice(s, s);
        }
        OptionData opsData = new OptionData(OptionType.STRING, "operations", "The possible operations on the resulting image/embed", true);
        Arrays.stream(LOONAParameters.Display.values()).map(Enum::toString).forEach(t -> opsData.addChoice(t, t));
        OptionData modsDatga = new OptionData(OptionType.STRING, "modes", "Whether the results will be shown grouped within the selector or not", true);
        Arrays.stream(LOONAParameters.Mode.values()).map(Enum::toString).forEach(t -> modsDatga.addChoice(t, t));
        OptionData targetData = new OptionData(OptionType.STRING, "target", "Whether the results contains info from all the server members or only the caller of the command.", true);
        Arrays.stream(LOONAParameters.Subject.values()).map(Enum::toString).forEach(t -> targetData.addChoice(t, t));
        return List.of(
                () ->
                        new ExplanationLine(
                                "group-selector",
                                "The first group means the selector you can use. You can either select all the members, a specific member,all subgroups, a specific subgroup,the main group or what is tagged as Misc.",
                                selectorData
                        ),
                () -> new ExplanationLine(
                        "Operations",
                        "The second group represents the different operations that can be done. COLLAGE draws a image with all the resulting artist of the previous selector. SUM returns a normal _whoknows_ with the aggregate of each user for the given selection. COUNT displays an embbed with the artist with the most listeners.",
                        opsData
                ),
                () -> new ExplanationLine(
                        "Modes",
                        "The third group means whether the results will be shown grouped within the selector or not. For example if I were to choose ***COLLAGE YYXY UNGROUPED***  I would get one image per artist tagged on last.fm under the Subunit YYXY. If I chose **GROUPED** only one result will appear.",
                        modsDatga
                ),
                () -> new ExplanationLine(
                        "Target",
                        "The last group means whether the results contains info from all the server members or only the caller of the command.",
                        targetData
                ), new CommandExplanation("The defaults are: ALL COLLAGE UNGROUPED SERVER"));
    }

}

