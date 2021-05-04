package core.parsers.params;

import core.commands.Context;

public class RateLimitParams extends CommandParameters {
    private final long discordId;
    private final Float rateLimit;


    public RateLimitParams(Context e, long discordId, Float rateLimit) {
        super(e);
        this.discordId = discordId;
        this.rateLimit = rateLimit;
    }

    public boolean isDeleting() {
        return hasOptional("delete");
    }

    public long getDiscordId() {
        return discordId;
    }

    public Float getRateLimit() {
        return rateLimit;
    }
}
