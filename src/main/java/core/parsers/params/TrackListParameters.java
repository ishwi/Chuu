package core.parsers.params;

import core.commands.Context;

public class TrackListParameters extends CommandParameters {
    private final boolean parse;

    public TrackListParameters(Context e, boolean parse) {
        super(e);
        this.parse = parse;
    }

    public boolean isParse() {
        return parse;
    }
}
