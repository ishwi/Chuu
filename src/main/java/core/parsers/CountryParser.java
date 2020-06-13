package core.parsers;

import com.neovisionaries.i18n.CountryCode;
import core.exceptions.InstanceNotFoundException;
import core.parsers.params.CountryParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

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
    protected CountryParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {
        ParserAux parserAux = new ParserAux(words);
        User sample = parserAux.getOneUser(e);
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
        CountryCode country;
        if (countryCode.length() == 2) {
            if (countryCode.equalsIgnoreCase("uk")) {
                countryCode = "gb";
            }
            country = CountryCode.getByAlpha2Code(countryCode.toUpperCase());
        } else if (countryCode.length() == 3) {
            if (countryCode.equalsIgnoreCase("eng")) {
                countryCode = "gb";
            }
            country = CountryCode.getByAlpha3Code(countryCode.toUpperCase());
        } else {
            if (countryCode.equalsIgnoreCase("england")) {
                countryCode = "gb";
                country = CountryCode.getByAlpha2Code(countryCode.toUpperCase());

            } else {
                String finalCountryCode = countryCode;
                Optional<Locale> opt = Arrays.stream(Locale.getISOCountries()).map(x -> new Locale("en", x)).
                        filter(y -> y.getDisplayCountry().equalsIgnoreCase(finalCountryCode))
                        .findFirst();
                if (opt.isPresent()) {
                    country = CountryCode.getByAlpha3Code(opt.get().getISO3Country());
                } else {
                    try {
                        List<CountryCode> byName = CountryCode.findByName(Pattern.compile(".*" + countryCode + ".*")).stream()
                                .filter(x -> !x.getName().equalsIgnoreCase("Undefined")).collect(Collectors.toList());

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
        // :pensive:
//        if (country == CountryCode.IL) {
//            // No political statement at all, just bugfixing
//            country = CountryCode.PS;
//        }
        return new CountryParameters(e, lastFMData, country, timeFrameEnum);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *country* *[d,w,m,q,s,y,a]* *username*** \n" +
                "\tIf the username it's not provided it defaults to authors account, only ping and tag format (user#number)" +
                "\n\tIf the timeframe it's not specified it defaults to All-Time" +
                "\n\tCountry must come in the full name format or in the ISO 3166-1 alpha-2/alpha-3" +
                " format\n ";
    }
}
