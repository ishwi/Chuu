package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.RandomAlbumParser;
import dao.ChuuService;
import dao.entities.RandomUrlEntity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class RandomAlbumCommand extends ConcurrentCommand {
    public RandomAlbumCommand(ChuuService dao) {
        super(dao);
        this.parser = new RandomAlbumParser();
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
    protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned;

        returned = parser.parse(e);
        if (returned == null)
            return;
        if (returned.length == 0) {
            //get randomurl
            RandomUrlEntity randomUrl = getService().getRandomUrl();
            if (randomUrl == null) {
                sendMessageQueue(e, "The pool of urls was empty, add one first!");
                return;
            }
            String sb = e.getAuthor().getAsMention() + ", here's your random recommendation\n" +
                        "**Posted by:** " +
                        CommandUtil.sanitizeUserString(getUserString(e, randomUrl.getDiscordId())) + "\n**Link:** " +
                        randomUrl.getUrl();
            e.getChannel().sendMessage(sb).queue();
            return;
        }
        //add url
        Long guildId = CommandUtil.getGuildIdConsideringPrivateChannel(e);

        if (!getService().addToRandomPool(new RandomUrlEntity(returned[0], e.getAuthor().getIdLong(), guildId))) {
            sendMessageQueue(e, "The provided url: " + returned[0] + " was already on the pool");
            return;
        }
        sendMessageQueue(e, "Successfully added " + getUserString(e, e.getAuthor().getIdLong(), e.getAuthor()
                .getName()) + "'s link to the pool");

    }

    @Override
    public String getName() {
        return "Random Url";
    }
}
