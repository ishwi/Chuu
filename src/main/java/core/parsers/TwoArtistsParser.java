package core.parsers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwoArtistsParser extends Parser {
    private static final Pattern twoArtists = Pattern.compile("(.+) (?:to:)(.+)");

    @Override
    protected void setUpErrorMessages() {
        super.errorMessages.put(1, "You need to introduce first the alias you want and next `to: artist_to_alias` \n e.g: `!alias Radohead to: Radiohead`");
    }

    @Override
    protected String[] parseLogic(MessageReceivedEvent e, String[] words) {
        String first;
        String second;

        String joined = String.join(" ", words);

        Matcher matcher = twoArtists.matcher(joined);
        if (matcher.matches()) {
            first = matcher.group(1).trim();
            second = matcher.group(2).trim();
            return new String[]{first, second};
        } else if (words.length == 2) {
            first = words[0];
            second = words[1];
            return new String[]{first, second};

        } else {
            sendError(getErrorMessage(1), e);
            return null;
        }
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *firstArtist* *to:* *secondArtist*** \n" +
               "\t It's also valid when the two artists are both one word long to write: " + commandName + " firstArtist secondArtist";
    }
}
