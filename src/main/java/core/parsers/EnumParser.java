package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.EnumParameters;
import core.util.Aliasable;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class EnumParser<T extends Enum<T>> extends Parser<EnumParameters<T>> {
    protected final Class<T> clazz;
    private final boolean allowEmpty;
    private final boolean hasParams;
    private final boolean continueOnNomatch;

    public EnumParser(Class<T> tClass) {
        this(tClass, false, false, false);
    }

    public EnumParser(Class<T> tClass, boolean allowEmpty, boolean hasParams, boolean continueOnNomatch) {
        this.clazz = tClass;
        this.allowEmpty = allowEmpty;
        this.hasParams = hasParams;
        this.continueOnNomatch = continueOnNomatch;
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    public EnumParameters<T> parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        List<String> lines = mapEnumToPosibilites();


        SlashCommandEvent e = ctx.e();
        Optional<String> option = Optional.ofNullable(e.getOption("option")).map(OptionMapping::getAsString);
        if (option.isEmpty()) {
            if (allowEmpty) {
                return new EnumParameters<>(ctx, null, null);
            }
            sendError("Pls introduce only one of the following: **" + String.join("**, **", lines) + "**", ctx);
            return null;
        }
        String param = Optional.ofNullable(e.getOption("parameter")).map(OptionMapping::getAsString).orElse(null);
        return new EnumParameters<>(ctx, option.map(z -> Enum.valueOf(clazz, z.toUpperCase().replaceAll("-", "_"))).orElse(null), param);
    }

    private List<String> mapEnumToPosibilites() {
        EnumSet<T> ts = EnumSet.allOf(clazz);
        return ts.stream().<String>mapMulti((x, consumer) -> {
            if (x instanceof Aliasable aliasable) {
                aliasable.aliases().stream().map(String::toLowerCase).forEach(consumer);
            }
            consumer.accept(x.name().replaceAll("_", "-").toLowerCase());
        }).toList();
    }

    @Override
    protected EnumParameters<T> parseLogic(Context e, String[] words) {
        List<String> lines = mapEnumToPosibilites();

        if (words.length == 0) {
            if (allowEmpty) {
                return new EnumParameters<>(e, null, null);
            }
            sendError("Pls introduce only one of the following: **" + String.join("**, **", lines) + "**", e);
            return null;
        } else if (words.length > 1) {
            if (!hasParams) {
                sendError("Pls introduce only one of the following: **" + String.join("**, **", lines) + "**", e);
                return null;
            }
        }

        Optional<String> first = lines.stream().filter(x -> words[0].equalsIgnoreCase(x)).findFirst();
        if (first.isEmpty()) {
            if (continueOnNomatch) {
                return new EnumParameters<>(e, null, String.join(" ", words));
            }
            sendError("Pls introduce one of the following: " + String.join(",", lines), e);
            return null;
        }
        String params;
        if (words.length > 1) {
            params = String.join(" ", Arrays.copyOfRange(words, 1, words.length));
        } else {
            params = null;
        }
        return new EnumParameters<>(e, Enum.valueOf(clazz, first.get().toUpperCase().replaceAll("-", "_")), params);


    }

    @Override
    public List<Explanation> getUsages() {
        List<String> lines = EnumSet.allOf(clazz).stream().map(x -> x.name().replaceAll("_", "-").toLowerCase()).toList();
        OptionData data = new OptionData(OptionType.STRING, "option", "One of the possible configuration values");
        if (!allowEmpty) {
            data.setRequired(true);
        }
        for (String line : lines) {
            data.addChoice(line, line);
        }

        Explanation option = () -> new ExplanationLine("option", "Option being one of: **" + String.join("**, **", lines) + "**", data);
        if (hasParams) {
            Explanation params = () -> new ExplanationLineType("parameter", "Parameter for option", OptionType.STRING);
            return List.of(option, params);
        }
        return List.of(option);
    }


}

