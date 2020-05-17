package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RateLimitParams extends CommandParameters {
    private final long discordId;
    private final Float rateLimit;


    public RateLimitParams(MessageReceivedEvent e, long discordId, Float rateLimit) {
        super(e);
        this.discordId = discordId;
        this.rateLimit = rateLimit;
    }

    public boolean isDeleting() {
        return hasOptional("--delete");
    }

    public long getDiscordId() {
        return discordId;
    }

    public Float getRateLimit() {
        return rateLimit;
    }
}
