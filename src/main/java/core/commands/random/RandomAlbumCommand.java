package core.commands.random;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.Parser;
import core.parsers.RandomAlbumParser;
import core.parsers.params.RandomUrlParameters;
import core.parsers.utils.OptionalEntity;
import core.util.ServiceView;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import dao.entities.RandomTarget;
import dao.entities.RandomUrlEntity;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class RandomAlbumCommand extends ConcurrentCommand<RandomUrlParameters> {
    public RandomAlbumCommand(ServiceView dao) {
        super(dao);
    }


    private @Nullable
    RandomTarget getRandomTarget(RandomUrlParameters parameters) {
        if (parameters.hasOptional("spotify")) {
            return RandomTarget.SPOTIFY;
        }
        if (parameters.hasOptional("bandcamp")) {
            return RandomTarget.BANDCAMP;
        }
        if (parameters.hasOptional("youtube")) {
            return RandomTarget.YOUTUBE;
        }
        if (parameters.hasOptional("soundcloud")) {
            return RandomTarget.SOUNDCLOUD;
        }
        if (parameters.hasOptional("deezer")) {
            return RandomTarget.DEEZER;
        }
        return null;
    }

    private String buildEmptyMessage(@Nullable RandomTarget randomTarget, String target) {
        String ending = Optional.ofNullable(randomTarget).map(w -> " from %s!".formatted(WordUtils.capitalizeFully(randomTarget.name()))).orElse("!");
        return "%s doesn't have any random url%s".formatted(target, ending);

    }

    @Override
    public String slashName() {
        return "obtain";
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RANDOM;
    }

    @Override
    public Parser<RandomUrlParameters> initParser() {
        return new RandomAlbumParser(db,
                new OptionalEntity("spotify", "only include spotify links", "sp"),
                new OptionalEntity("bandcamp", "only include spotify links", "bc"),
                new OptionalEntity("youtube", "only include spotify links", "yt"),
                new OptionalEntity("soundcloud", "only include spotify links", "sc"),
                new OptionalEntity("deezer", "only include spotify links", "dee")
        );
    }

    @Override
    public String getDescription() {
        return "Gets a random url that other users have added, or add one yourself";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("random");
    }

    @Override
    public void onCommand(Context e, @Nonnull RandomUrlParameters params) {


        String url = params.getUrl();
        if (url.length() == 0) {
            //get randomurl
            RandomUrlEntity randomUrl;
            RandomTarget randomTarget = getRandomTarget(params);
            if (params.hasOptional("server") && e.isFromGuild()) {
                randomUrl = db.getRandomUrlFromServer(e.getGuild().getIdLong(), randomTarget);
                if (randomUrl == null) {
                    String name = e.getGuild().getName();
                    sendMessageQueue(e, buildEmptyMessage(randomTarget, name));
                    return;
                }
            } else if (params.getUser().getIdLong() != e.getAuthor().getIdLong()) {
                randomUrl = db.getRandomUrlFromUser(params.getUser().getIdLong(), randomTarget);
                if (randomUrl == null) {
                    String userString = getUserString(e, params.getUser().getIdLong());
                    sendMessageQueue(e, buildEmptyMessage(randomTarget, userString));
                    return;
                }
            } else {
                randomUrl = db.getRandomUrl(randomTarget);
                if (randomUrl == null) {
                    sendMessageQueue(e, buildEmptyMessage(randomTarget, e.getJDA().getSelfUser().getName()));
                    return;
                }
            }

            String ownerRec = null;// getUserString(e, randomUrl.getDiscordId());
            if (randomUrl.discordId() != null && randomUrl.discordId() != e.getJDA().getSelfUser().getIdLong()) {
                try {
                    LastFMData lastFMData = db.findLastFMData(randomUrl.discordId());
                    PrivacyMode privacyMode = lastFMData.getPrivacyMode();
                    ownerRec = switch (privacyMode) {
                        case STRICT -> "Private User";
                        case DISCORD_NAME, NORMAL -> getUserString(e, lastFMData.getDiscordId());
                        case TAG -> e.getJDA().retrieveUserById(lastFMData.getDiscordId()).complete().getAsTag();
                        case LAST_NAME -> lastFMData.getName() + " (lastfm)";
                    };
                } catch (InstanceNotFoundException ex) {
                    ownerRec = "Unknown";
                }
            }
            if (ownerRec == null) {
                ownerRec = e.getJDA().getSelfUser().getName();
            }
            String sb = String.format("%s, here's your random recommendation%n**Posted by:** %s%n**Link:** %s", CommandUtil.escapeMarkdown(e.getAuthor().getName()), ownerRec, randomUrl.url());
            e.sendMessage(sb).queue();
            return;
        }
        //add url
        //dao.findLastFMData(e.getAuthor().getIdLong());

        if (!db.addToRandomPool(new RandomUrlEntity(url, e.getAuthor().getIdLong()))) {
            sendMessageQueue(e, String.format("The provided url: <%s> was already on the pool", url));
            return;
        }
        sendMessageQueue(e, String.format("Successfully added %s's link to the pool", getUserString(e, e.getAuthor().getIdLong(), CommandUtil.escapeMarkdown(e.getAuthor()
                .getName()))));

    }

    @Override
    public String getName() {
        return "Random Url";
    }
}
