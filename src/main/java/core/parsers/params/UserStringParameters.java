package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;

public class UserStringParameters extends ChuuDataParams {
    private final String value;

    public UserStringParameters(Context e, LastFMData lastFMData, String value) {
        super(e, lastFMData);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
