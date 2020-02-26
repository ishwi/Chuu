package core.parsers;

import com.neovisionaries.i18n.CountryCode;
import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CountryParser extends DaoParser {
    public CountryParser(ChuuService dao) {
        super(dao);
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(5, "You forgot to input a country. You can try the full country name or the 2/3 ISO code");
        errorMessages.put(6, "Coudnt find any country named like that");

    }

    @Override
    protected String[] parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {
        User sample;

        if (e.isFromGuild()) {
            List<Member> members = e.getMessage().getMentionedMembers();
            if (!members.isEmpty()) {
                if (members.size() != 1) {
                    sendError("Only one user pls", e);
                    return null;
                }
                sample = members.get(0).getUser();
                words = Arrays.stream(words).filter(s -> !s.equals(sample.getAsMention()) && !s.equals("<@!" + sample.getAsMention().substring(2))).toArray(String[]::new);
            } else {
                sample = e.getMember().getUser();
            }
        } else
            sample = e.getAuthor();

        ChartParserAux chartParserAux = new ChartParserAux(words);
        TimeFrameEnum timeFrameEnum = chartParserAux.parseTimeframe(TimeFrameEnum.ALL);
        words = chartParserAux.getMessage();
        if (words.length == 0) {
            sendError(getErrorMessage(5), e);
            return null;
        }
        String countryCode = String.join(" ", words);
        CountryCode country;
        if (countryCode.length() == 2) {
            country = CountryCode.getByAlpha2Code(countryCode.toUpperCase());
        } else if (countryCode.length() == 3) {
            country = CountryCode.getByAlpha3Code(countryCode.toUpperCase());
        } else {
            Optional<Locale> opt = Arrays.stream(Locale.getISOCountries()).map(x -> new Locale("en", x)).
                    filter(y -> y.getDisplayCountry().equalsIgnoreCase(countryCode))
                    .findFirst();
            if (opt.isPresent()) {
                country = CountryCode.getByAlpha3Code(opt.get().getISO3Country());
            } else {
                List<CountryCode> byName = CountryCode.findByName(countryCode);
                if (byName.isEmpty()) {
                    country = null;
                } else {
                    country = byName.get(0);
                }
            }
        }
        if (country == null) {
            sendError(getErrorMessage(6), e);
            return null;
        }

        LastFMData lastFMData = dao.findLastFMData(sample.getIdLong());
        return new String[]{lastFMData.getName(), String.valueOf(sample.getIdLong()), country.getAlpha2(), timeFrameEnum.toApiFormat()};
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *COUNTRY*  *timeframe* *username* " +
               "\n\tif username its not specified it defaults to you" +
               "\n\tif timeframe its not specified it defaults to All Time" +
               "\n\tCountry must come in the full name format or in the  ISO 3166-1 alpha-2/alpha-3" +
               " format ";
    }
}
