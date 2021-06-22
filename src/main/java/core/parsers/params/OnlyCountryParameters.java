package core.parsers.params;

import com.neovisionaries.i18n.CountryCode;
import core.commands.Context;

public class OnlyCountryParameters extends CommandParameters {
    private final CountryCode code;

    public OnlyCountryParameters(Context e, CountryCode code) {
        super(e);
        this.code = code;
    }

    public CountryCode getCode() {
        return code;
    }
}
