package core.parsers;

import com.neovisionaries.i18n.CountryCode;
import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.CountryExplanation;
import core.parsers.explanation.TimeframeExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.CountryParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CountryParser extends DaoParser<CountryParameters> {

    public CountryParser(ChuuService dao) {
        super(dao);
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(5, "You didn't introduce anything. You can try the full country name or the 2/3 ISO code");
        errorMessages.put(6, "Could not find any country named like that");

    }

    @Override
    public CountryParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        TimeFrameEnum timeFrameEnum = InteractionAux.parseTimeFrame(e, TimeFrameEnum.ALL);
        User user = InteractionAux.parseUser(e);
        LastFMData data = findLastfmFromID(user, ctx);
        OptionMapping country = e.getOption("country");
        CountryCode countryCode = fromString(ctx, country.getAsString());
        if (countryCode == null) return null;
        return new CountryParameters(ctx, data, countryCode, new CustomTimeFrame(timeFrameEnum));
    }

    @Override
    protected CountryParameters parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        ParserAux parserAux = new ParserAux(words);
        User sample = parserAux.getOneUser(e, dao);
        words = parserAux.getMessage();
        LastFMData lastFMData = findLastfmFromID(sample, e);
        ChartParserAux chartParserAux = new ChartParserAux(words);
        TimeFrameEnum timeFrameEnum = chartParserAux.parseTimeframe(TimeFrameEnum.ALL);
        words = chartParserAux.getMessage();
        String countryCode;
        if (words.length == 0) {
            sendError(getErrorMessage(5), e);
            return null;
        } else {
            countryCode = String.join(" ", words);
        }
        CountryCode country = fromString(e, countryCode);
        if (country == null) return null;
        // :pensive:
//        if (country == CountryCode.IL) {
//            // No political statement at all, just bugfixing
//            country = CountryCode.PS;
//        }
        return new

                CountryParameters(e, lastFMData, country, new CustomTimeFrame(timeFrameEnum));

    }

    @Nullable
    private CountryCode fromString(Context e, String value) {
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
            } else if (value.equalsIgnoreCase("eSwatini ")) {
                value = "sz";
                country = CountryCode.getByAlpha2Code(value.toUpperCase());
            } else {
                String finalCountryCode = value;
                Optional<Locale> opt = Arrays.stream(Locale.getISOCountries()).map(x -> new Locale("en", x)).
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
                        sendError("The provided regex is not a valid one according to Java :(", e);
                        return null;
                    }
                }
            }
        }
        if (country == null) {
            sendError(getErrorMessage(6), e);
            return null;
        }
        return country;
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(InteractionAux.required(new CountryExplanation()), new TimeframeExplanation(TimeFrameEnum.ALL));
    }

}
