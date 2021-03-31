package core.parsers;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.TwoArtistParams;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwoArtistsParser extends Parser<TwoArtistParams> {
    private static final Pattern twoArtists = Pattern.compile("(.+) to:(.+)");

    @Override
    protected void setUpErrorMessages() {
        super.errorMessages.put(1, "You need to introduce first the alias you want and next `to: artist_to_alias` \n e.g: `!alias Radohead to: Radiohead`");
    }

    @Override
    protected TwoArtistParams parseLogic(MessageReceivedEvent e, String[] words) {
        String first;
        String second;

        String joined = String.join(" ", words);

        Matcher matcher = twoArtists.matcher(joined);
        if (matcher.matches()) {
            first = matcher.group(1).trim();
            second = matcher.group(2).trim();
            return new TwoArtistParams(e, first, second);
        } else if (words.length == 2) {
            first = words[0];
            second = words[1];
            return new TwoArtistParams(e, first, second);
        } else {
            sendError(getErrorMessage(1), e);
            return null;
        }
    }

    @Override
    public List<Explanation> getUsages() {
        return Collections.singletonList(() -> new ExplanationLine("firstArtist to: secondArtist", "It's also valid when the two artists are both one word long to write them without the to:"));
    }

}
