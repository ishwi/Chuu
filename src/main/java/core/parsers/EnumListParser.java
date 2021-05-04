package core.parsers;

import core.commands.Context;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.EnumListParameters;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public class EnumListParser<T extends Enum<T>> extends Parser<EnumListParameters<T>> {
    protected final Class<T> clazz;
    private final String name;
    private final EnumSet<T> excluded;
    private final Function<String, EnumSet<T>> mapper;


    public EnumListParser(String name, Class<T> tClass, EnumSet<T> excluded, Function<String, EnumSet<T>> mapper) {
        this.name = name;
        this.clazz = tClass;
        this.excluded = excluded;
        this.mapper = mapper;
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected EnumListParameters<T> parseLogic(Context e, String[] words) {
        EnumSet<T> building = EnumSet.noneOf(clazz);

        if (words.length == 0) {
            return new EnumListParameters<>(e, building, false, true);
        }

        if (words[0].equalsIgnoreCase("help")) {

            if (words.length > 1) {
                if (words[1].equals("all")) {
                    building = EnumSet.complementOf(excluded);
                } else {
                    String remaining = String.join(" ", Arrays.copyOfRange(words, 1, words.length));
                    building = mapper.apply(remaining);
                }
            }
            return new EnumListParameters<>(e, building, true, false);
        } else if (words[0].equalsIgnoreCase("list")) {
            return new EnumListParameters<>(e, building, false, true);
        } else {
            String remaining = String.join(" ", Arrays.copyOfRange(words, 0, words.length));
            building = mapper.apply(remaining);
            if (building.isEmpty()) {
                return new EnumListParameters<>(e, building, true, true);
            }
            return new EnumListParameters<>(e, building, false, false);
        }
    }

    @Override
    public List<Explanation> getUsages() {
        EnumSet<T> set = EnumSet.complementOf(excluded);
        List<String> lines = set.stream().map(x -> WordUtils.capitalizeFully(x.name().replaceAll("_", "-"), '-')).toList();
        String join = String.join("** | **", lines);
        String usage = "\t Writing **__help__** will give you a brief description of all the " + name + " that you include in the command or alternatively all the options with **__help__**\n" +
                       "\t Writing **__list__** will give you all your current set " + name + "\n";
        OptionData optionData = new OptionData(OptionType.STRING, "auxiliar", "auxiliar options");
        optionData.addChoice("help", "help");
        optionData.addChoice("help-all", "help-all");
        optionData.addChoice("list", "list");
        OptionData optionData2 = new OptionData(OptionType.STRING, "values", "the values to select");
        lines.stream().limit(25).forEach(t -> optionData2.addChoice(t, t));
        Explanation explanation = () -> new ExplanationLine("[help|help all|list|] " + name, usage, optionData);
        Explanation explanation2 = () -> new ExplanationLine("config value ", join, optionData2);
        return List.of(explanation, explanation2);


    }

}
