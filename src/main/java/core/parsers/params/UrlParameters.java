package core.parsers.params;

import core.commands.Context;

public class UrlParameters extends CommandParameters {
    private final String url;

    public UrlParameters(Context e, String url) {
        super(e);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
