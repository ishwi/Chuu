package core.parsers;

import com.neovisionaries.i18n.CountryCode;
import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.CountryExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.OnlyCountryParameters;
import core.parsers.utils.CountryParse;
import dao.ChuuService;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class OnlyCountryParser extends DaoParser<OnlyCountryParameters> {

    public OnlyCountryParser(ChuuService dao) {
        super(dao);
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(5, "You didn't introduce anything. You can try the full country name or the 2/3 ISO code");
        errorMessages.put(6, "Could not find any country named like that");
    }

    @Override
    public OnlyCountryParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) {
        CommandInteraction e = ctx.e();
        OptionMapping country = e.getOption("country");
        CountryCode countryCode = CountryParse.fromString(this, ctx, country.getAsString());
        if (countryCode == null) return null;
        return new OnlyCountryParameters(ctx, countryCode);
    }

    @Override
    protected OnlyCountryParameters parseLogic(Context e, String[] words) {
        String countryCode;
        if (words.length == 0) {
            sendError(getErrorMessage(5), e);
            return null;
        } else {
            countryCode = String.join(" ", words);
        }
        CountryCode country = CountryParse.fromString(this, e, countryCode);
        if (country == null) return null;
        return new OnlyCountryParameters(e, country);

    }


    @Override
    public List<Explanation> getUsages() {
        return List.of(InteractionAux.required(new CountryExplanation()));
    }

}
