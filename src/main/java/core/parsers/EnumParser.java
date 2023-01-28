package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.EnumParameters;
import core.util.Aliasable;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.*;

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
    public EnumParameters<T> parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) {
        Map<String, String> lineToValue = mapEnumToPosibilites();
        List<String> lines = lineToValue.keySet().stream().toList();


        CommandInteraction e = ctx.e();
        Optional<String> option = Optional.ofNullable(e.getOption("option")).map(OptionMapping::getAsString);
        if (option.isEmpty()) {
            if (allowEmpty) {
                return new EnumParameters<>(ctx, null, null);
            }
            sendError("Pls introduce only one of the following: **" + String.join("**, **", lines) + "**", ctx);
            return null;
        }
        String param = Optional.ofNullable(e.getOption("parameter")).map(OptionMapping::getAsString).orElse(null);
        return new EnumParameters<>(ctx, option.map(z -> Enum.valueOf(clazz, lineToValue.get(z).toUpperCase().replaceAll("-", "_"))).orElse(null), param);
    }

    private Map<String, String> mapEnumToPosibilites() {
        Map<String, String> map = new HashMap<>();

        EnumSet.allOf(clazz)
                .forEach((x) -> {
                    String value = x.name().replaceAll("_", "-").toLowerCase();
                    if (x instanceof Aliasable aliasable) {
                        aliasable.aliases().stream().map(String::toLowerCase).forEach(z -> map.put(z, value));
                    }
                    map.put(value, value);
                });
        return map;
    }

    @Override
    protected EnumParameters<T> parseLogic(Context e, String[] words) {
        Map<String, String> mapLine = mapEnumToPosibilites();
        Set<String> lines = mapLine.keySet();

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
        return new EnumParameters<>(e, Enum.valueOf(clazz, mapLine.get(first.get()).toUpperCase().replaceAll("-", "_")), params);


    }

    @Override
    public List<Explanation> getUsages() {
        List<String> lines = EnumSet.allOf(clazz).stream().map(x -> x.name().replaceAll("_", "-").toLowerCase()).toList();
        OptionData data = new OptionData(OptionType.STRING, "value", "One of the possible configuration values");
        if (!allowEmpty) {
            data.setRequired(true);
        }
        for (String line : lines) {
            data.addChoice(line, line);
        }

        Explanation option = () -> new ExplanationLine("value", "Option being one of: **" + String.join("**, **", lines) + "**", data);
        if (hasParams) {
            Explanation params = () -> new ExplanationLineType("parameter", "Parameter for option", OptionType.STRING);
            return List.of(option, params);
        }
        return List.of(option);
    }


}

