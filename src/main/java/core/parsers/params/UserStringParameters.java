package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;

public class UserStringParameters extends ChuuDataParams {
    private final String input;

    public UserStringParameters(Context e, LastFMData lastFMData, String input) {
        super(e, lastFMData);
        this.input = input;
    }


    public String getInput() {
        return input;
    }
}
