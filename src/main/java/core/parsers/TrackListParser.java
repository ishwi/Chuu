package core.parsers;

import core.commands.Context;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.TrackListParameters;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TrackListParser extends Parser<TrackListParameters> {
    private final Pattern linePattern = Pattern.compile("^(\\d+\\. )(.*)$");

    @Override
    protected TrackListParameters parseLogic(Context e, String[] words) {
        if (hasOptional("spotify", e) || hasOptional("musicbrainz", e) || hasOptional("lastfm", e)) {
            return new TrackListParameters(e, false);
        }
        String message = String.join(" ", words);
        String[] line = message.split("\n");

        return new TrackListParameters(e, true);
    }

    @Override
    public List<Explanation> getUsages() {
        return Collections.emptyList();
    }
}
