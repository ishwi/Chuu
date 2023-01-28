package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.params.TwoArtistParams;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
    public TwoArtistParams parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) {
        CommandInteraction e = ctx.e();
        String alias = e.getOption("alias").getAsString();
        String existingArtist = e.getOption("existing-artist").getAsString();
        return new TwoArtistParams(ctx, alias, existingArtist);
    }

    @Override
    protected TwoArtistParams parseLogic(Context e, String[] words) {
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
        OptionData al = new OptionData(OptionType.STRING, "alias", "New alias to add", true);
        OptionData existingArtist = new OptionData(OptionType.STRING, "existing-artist", "Existing artist this new alias will point to", true);
        return Collections.singletonList(() -> new ExplanationLine("new-alias to: artist", "It's also valid when the two artists are both one word long to write them without the to:", List.of(al, existingArtist)));
    }

}
