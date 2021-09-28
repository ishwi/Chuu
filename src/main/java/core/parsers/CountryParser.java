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
import core.parsers.utils.CountryParse;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

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
        CountryCode countryCode = CountryParse.fromString(this, ctx, country.getAsString());
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
        CountryCode country = CountryParse.fromString(this, e, countryCode);
        if (country == null) return null;
        // :pensive:
//        if (country == CountryCode.IL) {
//            // No political statement at all, just bugfixing
//            country = CountryCode.PS;
//        }
        return new

                CountryParameters(e, lastFMData, country, new CustomTimeFrame(timeFrameEnum));

    }


    @Override
    public List<Explanation> getUsages() {
        return List.of(InteractionAux.required(new CountryExplanation()), new TimeframeExplanation(TimeFrameEnum.ALL));
    }

}
