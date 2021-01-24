package core.commands.discovery;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.Parser;
import core.parsers.RandomAlbumParser;
import core.parsers.params.UrlParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import dao.entities.RandomUrlEntity;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class RandomAlbumCommand extends ConcurrentCommand<UrlParameters> {
    public RandomAlbumCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<UrlParameters> initParser() {
        return new RandomAlbumParser();
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
    protected void onCommand(MessageReceivedEvent e, @NotNull UrlParameters params) {


        String url = params.getUrl();
        if (url.length() == 0) {
            //get randomurl
            RandomUrlEntity randomUrl;
            if (params.hasOptional("server") && e.isFromGuild()) {
                randomUrl = db.getRandomUrlFromServer(e.getGuild().getIdLong());
            } else {
                randomUrl = db.getRandomUrl();
            }
            if (randomUrl == null) {
                sendMessageQueue(e, "The pool of urls was empty, add one first!");
                return;
            }
            String ownerRec = null;// getUserString(e, randomUrl.getDiscordId());
            if (randomUrl.getDiscordId() != null && randomUrl.getDiscordId() != e.getJDA().getSelfUser().getIdLong()) {
                try {
                    LastFMData lastFMData = db.findLastFMData(randomUrl.getDiscordId());
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
            String sb = String.format("%s, here's your random recommendation%n**Posted by:** %s%n**Link:** %s", CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName()), ownerRec, randomUrl.getUrl());
            e.getChannel().sendMessage(sb).queue();
            return;
        }
        //add url
        Long guildId = CommandUtil.getGuildIdConsideringPrivateChannel(e);
        //db.findLastFMData(e.getAuthor().getIdLong());

        if (!db.addToRandomPool(new RandomUrlEntity(url, e.getAuthor().getIdLong(), guildId))) {
            sendMessageQueue(e, String.format("The provided url: %s was already on the pool", url));
            return;
        }
        sendMessageQueue(e, String.format("Successfully added %s's link to the pool", getUserString(e, e.getAuthor().getIdLong(), CommandUtil.cleanMarkdownCharacter(e.getAuthor()
                .getName()))));

    }

    @Override
    public String getName() {
        return "Random Url";
    }
}
