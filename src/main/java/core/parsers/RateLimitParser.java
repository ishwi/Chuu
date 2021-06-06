package core.parsers;

import core.commands.Context;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.RateLimitParams;
import dao.ChuuService;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.regex.Pattern;

public class RateLimitParser extends DaoParser<RateLimitParams> {
    private final Pattern pattern = Pattern.compile("\\d{15,20}");

    public RateLimitParser(ChuuService dao, OptionalEntity... opts) {
        super(dao, opts);
        this.opts.add(new OptionalEntity("delete", "remove the rate limit from the specific user"));
    }

    @Override
    protected RateLimitParams parseLogic(Context e, String[] words) {
        if (words.length != 1 && words.length != 2) {
            sendError("A raw discordId is expected, optionally with a new rate limit to apply to that user", e);
            return null;
        }
        if (words.length == 1) {
            if (pattern.matcher(words[0]).matches()) {
                long l = Long.parseLong(words[0]);
                return new RateLimitParams(e, l, null);
            }
            sendError("A raw discordId is expected, optionally with a new rate limit to apply to that user", e);
            return null;
        }
        boolean flagId = false;
        boolean flagRatelimit = false;
        long discordId = -1;
        Float flagRateLimit = null;
        for (String word : words) {
            if (!flagId && pattern.matcher(word).matches()) {
                discordId = Long.parseLong(word);
                flagId = true;
                continue;
            }
            if (!flagRatelimit) {
                try {
                    flagRateLimit = Float.parseFloat(word);
                    if (flagRateLimit < 0.01) {
                        sendError("The new Rate Limit must be bigger than 0.01", e);
                        return null;
                    }
                    flagRatelimit = true;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (!flagId || discordId == -1L) {
            sendError("A raw discordId is expected, optionally with a new rate limit to apply to that user", e);
            return null;
        }
        return new RateLimitParams(e, discordId, flagRateLimit);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(InteractionAux.required(() -> new ExplanationLineType("Discord id", "discord_id is the 20 long identifier of an user", OptionType.USER)),
                () -> new ExplanationLineType("Rate", "The rate is the number of request the user can do per second. Must be bigger than 0.01", OptionType.STRING)
        );
    }

}
