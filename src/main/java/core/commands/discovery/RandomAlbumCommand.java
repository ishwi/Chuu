package core.commands.discovery;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.Parser;
import core.parsers.RandomAlbumParser;
import core.parsers.params.RandomUrlParameters;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import dao.entities.RandomUrlEntity;
import dao.exceptions.InstanceNotFoundException;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class RandomAlbumCommand extends ConcurrentCommand<RandomUrlParameters> {
    public RandomAlbumCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<RandomUrlParameters> initParser() {
        return new RandomAlbumParser(db);
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
    protected void onCommand(Context e, @NotNull RandomUrlParameters params) {


        String url = params.getUrl();
        if (url.length() == 0) {
            //get randomurl
            RandomUrlEntity randomUrl;
            if (params.hasOptional("server") && e.isFromGuild()) {
                randomUrl = db.getRandomUrlFromServer(e.getGuild().getIdLong());
                if (randomUrl == null) {
                    String name = e.getGuild().getName();
                    sendMessageQueue(e, name + " doesn't have any submitted url!");
                    return;
                }
            } else if (params.getUser().getIdLong() != e.getAuthor().getIdLong()) {
                randomUrl = db.getRandomUrlFromUser(params.getUser().getIdLong());
                if (randomUrl == null) {
                    String userString = getUserString(e, params.getUser().getIdLong());
                    sendMessageQueue(e, userString + " hasn't submitted any url!");
                    return;
                }
            } else {
                randomUrl = db.getRandomUrl();
                if (randomUrl == null) {
                    sendMessageQueue(e, "The pool of urls was empty, add one first!");
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
                        case TAG -> e.getJDA().retrieveUserById(lastFMData.getDiscordId(), false).complete().getAsTag();
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
        //db.findLastFMData(e.getAuthor().getIdLong());

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
