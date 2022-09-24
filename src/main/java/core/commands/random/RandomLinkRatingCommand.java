package core.commands.random;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.RandomAlbumParser;
import core.parsers.params.NumberParameters;
import core.parsers.params.RandomUrlParameters;
import core.util.ServiceView;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import dao.entities.RandomUrlEntity;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class RandomLinkRatingCommand extends ConcurrentCommand<NumberParameters<RandomUrlParameters>> {
    public RandomLinkRatingCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RANDOM;
    }

    @Override
    public Parser<NumberParameters<RandomUrlParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be between 0 and 10");
        String s = "You should introduce a number to rate the given url";
        return new NumberParser<>(new RandomAlbumParser(db),
                null,
                10,
                map, s, false, true, true, "rating");
    }

    @Override
    public String getDescription() {
        return "Rate a url of the random pool";
    }


    @Override
    public String slashName() {
        return "rate";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rate");
    }

    @Override
    public String getName() {
        return "Random Rating";
    }

    @Override
    public void onCommand(Context e, @Nonnull NumberParameters<RandomUrlParameters> params) {


        Long rating = params.getExtraParam();
        String url = params.getInnerParams().getUrl();
        if (url.isBlank() || rating == null) {
            sendMessageQueue(e, "You must introduce a rating and the url to rate");
            return;
        }
        RandomUrlEntity randomUrl = db.findRandomUrl(url);
        if (randomUrl == null) {
            char messagePrefix = CommandUtil.getMessagePrefix(e);
            sendMessageQueue(e, "The given url was not in the pool therefore it cannot be rated. You might also consider adding it using the " + messagePrefix + "random .");
            return;
        }
        if (randomUrl.discordId().equals(e.getAuthor().getIdLong())) {
            sendMessageQueue(e, "You submitted this url so you are not allowed to rate it");
            return;
        }
        db.addUrlRating(e.getAuthor().getIdLong(), Math.toIntExact(rating), url);
        long discordId = randomUrl.discordId();
        try {
            LastFMData lastFMData = db.findLastFMData(discordId);
            if (lastFMData.isImageNotify()) {
                LastFMData ratingOwner = db.findLastFMData(e.getAuthor().getIdLong());
                PrivacyMode privacyMode = ratingOwner.getPrivacyMode();
                String s;
                switch (privacyMode) {
                    case NORMAL, STRICT -> s = "A private user";
                    case TAG -> {
                        User userById = e.getJDA().getUserById(ratingOwner.getDiscordId());
                        s = userById == null ? "A private user" : userById.getAsTag();
                    }
                    case LAST_NAME -> s = ratingOwner.getName() + " (last.fm)";
                    case DISCORD_NAME -> s = getUserString(e, ratingOwner.getDiscordId());
                    default -> throw new IllegalStateException("Unexpected value: " + privacyMode);
                }
                String finalS = s;
                e.getJDA().retrieveUserById(lastFMData.getDiscordId()).flatMap(User::openPrivateChannel).flatMap(x -> x.sendMessage(finalS + " has rated your random url " + url + " with a **" + rating + "**\nYou can disable this automated message with the config command")).queue();
            }

        } catch (
                InstanceNotFoundException ex) {
            // Well
        }
        sendMessageQueue(e, "Successfully rated the url with a %d/10!".formatted(rating));
    }
}
