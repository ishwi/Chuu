package core.commands;

import com.google.common.util.concurrent.RateLimiter;
import core.Chuu;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.RateLimitParser;
import core.parsers.params.RateLimitParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Map;

public class RateLimitCommand extends ConcurrentCommand<RateLimitParams> {
    public RateLimitCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<RateLimitParams> initParser() {
        return new RateLimitParser(getService());
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        RateLimitParams parse = parser.parse(e);

        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = getService().findLastFMData(idLong);
        if (!lastFMData.getRole().equals(Role.ADMIN)) {

            sendMessageQueue(e, "Only bot admins can modify rate limits");
            return;
        }
        if (parse == null) {
            return;
        }
        Map<Long, RateLimiter> ratelimited = Chuu.getRatelimited();
        long discordId = parse.getDiscordId();
        try {
            User complete = e.getJDA().retrieveUserById(discordId).complete();
        } catch (Exception ex) {
            sendMessageQueue(e, "Couldn't find any user with id " + discordId);
            return;

        }
        if (parse.isDeleting()) {
            ratelimited.remove(discordId);
            getService().removeRateLimit(discordId);
            sendMessageQueue(e, "Successfully removed the rate limit from user " + discordId);
            return;
        }
        Float rateLimit = parse.getRateLimit();
        RateLimiter rateLimiter = ratelimited.get(discordId);
        RateLimiter newRateLimiter = rateLimit == null ? (rateLimiter == null ? RateLimiter.create(0.1) : rateLimiter) : RateLimiter.create(rateLimit);
        ratelimited.put(discordId, newRateLimiter);
        getService().addRateLimit(discordId, (float) newRateLimiter.getRate());
        sendMessageQueue(e, "Successfully added a rate limit to user " + discordId);
    }
}
