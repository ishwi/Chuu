package core.parsers.explanation;

import com.neovisionaries.i18n.CountryCode;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineTypeAutocomplete;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class CountryExplanation implements Explanation {

    public static List<Command.Choice> searchChoices(CommandAutoCompleteInteractionEvent e) {
        String search = e.getFocusedOption().getValue();
        if (search.isBlank()) {
            return EnumSet.complementOf(EnumSet.of(CountryCode.UNDEFINED)).stream().limit(25).map(x -> new Command.Choice(x.getName(), x.getAlpha2())).toList();
        }
        return findAny(Pattern.compile(".*" + search + ".*", Pattern.CASE_INSENSITIVE)).stream().limit(25).map(x -> new Command.Choice(x.getName(), x.getAlpha2())).toList();


    }

    public static Collection<CountryCode> findAny(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null.");
        }

        Set<CountryCode> list = new LinkedHashSet<>();
        EnumSet<CountryCode> values = EnumSet.complementOf(EnumSet.of(CountryCode.UNDEFINED));
        for (CountryCode entry : values) {
            if (pattern.matcher(entry.getAlpha2()).matches()) {
                list.add(entry);
            }
        }
        for (CountryCode entry : values) {
            String alpha3 = entry.getAlpha3();
            if (!StringUtils.isBlank(alpha3)) {
                if (pattern.matcher(alpha3).matches()) {
                    list.add(entry);
                }
            }
        }
        for (CountryCode entry : values) {
            if (pattern.matcher(entry.getName()).matches()) {
                list.add(entry);
            }
        }
        return Collections.unmodifiableCollection(list);
    }

    @Override
    public Interactible explanation() {
        return new ExplanationLineTypeAutocomplete("country",
                "Country must come in the full name format or in the ISO 3166-1 alpha-2/alpha-3 format", OptionType.STRING, CountryExplanation::searchChoices);
    }
}
