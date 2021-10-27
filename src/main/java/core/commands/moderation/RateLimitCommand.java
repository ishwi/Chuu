package core.commands.moderation;

import com.google.common.util.concurrent.RateLimiter;
import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.RateLimitParser;
import core.parsers.params.RateLimitParams;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class RateLimitCommand extends ConcurrentCommand<RateLimitParams> {
    public RateLimitCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<RateLimitParams> initParser() {
        return new RateLimitParser(db);
    }

    @Override
    public String getDescription() {
        return "Allows bot admins to limit the number of request of some users";
    }

    @Override
    public List<String> getAliases() {
        return List.of("ratelimit");
    }

    @Override
    public String getName() {
        return "Rate Limiter";
    }

    @Override
    public void onCommand(Context e, @Nonnull RateLimitParams params) throws InstanceNotFoundException {


        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(idLong);
        if (!lastFMData.getRole().equals(Role.ADMIN)) {

            sendMessageQueue(e, "Only bot admins can modify rate limits");
            return;
        }

        Map<Long, RateLimiter> ratelimited = Chuu.getRatelimited();
        long discordId = params.getDiscordId();
        e.getJDA().retrieveUserById(discordId).queue(x -> handleUser(e, params, ratelimited, discordId), throwable -> sendMessageQueue(e, "Couldn't find any user with id " + discordId));
    }

    private void handleUser(Context e, RateLimitParams params, Map<Long, RateLimiter> ratelimited, long discordId) {
        if (params.isDeleting()) {
            ratelimited.remove(discordId);
            db.removeRateLimit(discordId);
            sendMessageQueue(e, "Successfully removed the rate limit from user " + discordId);
            return;
        }
        Float rateLimit = params.getRateLimit();
        RateLimiter rateLimiter = ratelimited.get(discordId);
        RateLimiter newRateLimiter = rateLimit == null ? (rateLimiter == null ? RateLimiter.create(0.1) : rateLimiter) : RateLimiter.create(rateLimit);
        ratelimited.put(discordId, newRateLimiter);
        db.addRateLimit(discordId, (float) newRateLimiter.getRate());
        sendMessageQueue(e, "Successfully added a rate limit to user " + discordId);
    }
}
