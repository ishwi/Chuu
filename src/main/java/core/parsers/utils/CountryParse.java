package core.parsers.utils;

import com.neovisionaries.i18n.CountryCode;
import core.commands.Context;
import core.parsers.Parser;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CountryParse {
    private CountryParse() {
    }

    @Nullable
    public static CountryCode fromString(Parser<?> parser, Context e, String value) {
        CountryCode country;
        if (value.length() == 2) {
            if (value.equalsIgnoreCase("uk")) {
                value = "gb";
            }
            country = CountryCode.getByAlpha2Code(value.toUpperCase());
        } else if (value.length() == 3) {
            if (value.equalsIgnoreCase("eng")) {
                value = "gb";
            }
            country = CountryCode.getByAlpha3Code(value.toUpperCase());
        } else {
            if (value.equalsIgnoreCase("england")) {
                value = "gb";
                country = CountryCode.getByAlpha2Code(value.toUpperCase());

            } else if (value.equalsIgnoreCase("northen macedonia")) {
                value = "mk";
                country = CountryCode.getByAlpha2Code(value.toUpperCase());
            } else if (value.equalsIgnoreCase("eSwatini")) {
                value = "sz";
                country = CountryCode.getByAlpha2Code(value.toUpperCase());
            } else {
                String finalCountryCode = value;
                Optional<Locale> opt = Arrays.stream(Locale.getISOCountries()).map(x -> Locale.of("en", x)).
                        filter(y -> y.getDisplayCountry().equalsIgnoreCase(finalCountryCode))
                        .findFirst();
                if (opt.isPresent()) {
                    country = CountryCode.getByAlpha3Code(opt.get().getISO3Country());
                } else {
                    try {
                        List<CountryCode> byName = CountryCode.findByName(Pattern.compile(".*" + value + ".*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)).stream()
                                .filter(x -> !x.getName().equalsIgnoreCase("Undefined")).toList();

                        if (byName.isEmpty()) {
                            country = null;
                        } else {
                            country = byName.get(0);
                        }
                    } catch (PatternSyntaxException ex) {
                        parser.sendError("The provided regex is not a valid one according to Java :(", e);
                        return null;
                    }
                }
            }
        }
        if (country == null) {
            parser.sendError(parser.getErrorMessage(6), e);
            return null;
        }
        return country;
    }
}
