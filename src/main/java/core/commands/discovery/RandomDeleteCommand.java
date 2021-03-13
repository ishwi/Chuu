package core.commands.discovery;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.RandomAlbumParser;
import core.parsers.params.UrlParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.RandomUrlDetails;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.List;

public class RandomDeleteCommand extends ConcurrentCommand<UrlParameters> {
    public RandomDeleteCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<UrlParameters> initParser() {
        return new RandomAlbumParser();
    }

    @Override
    public String getDescription() {
        return "Deletes a random url that you have posted";
    }

    @Override
    public List<String> getAliases() {
        return List.of("randomdelete", "randomdel");
    }

    @Override
    public String getName() {
        return "Deletes a random url";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull UrlParameters params) throws InstanceNotFoundException {


        String url = params.getUrl();
        if (StringUtils.isEmpty(url)) {
            sendMessageQueue(e, "Try to give an url to this command.");
            return;
        }
        RandomUrlDetails randomUrl = db.findRandomUrlDetails(url);
        if (randomUrl == null) {
            sendMessageQueue(e, "The given url was not in the pool therefore it cannot be deleted.");
            return;
        }
        LastFMData own = db.findLastFMData(e.getAuthor().getIdLong());

        if (randomUrl.discordId() != e.getAuthor().getIdLong()) {
            if (own.getRole() != Role.ADMIN) {
                sendMessageQueue(e, "Not your random url so you cannot delete it");
                return;
            }
        }
        db.truncateRandomPool();
        sendMessageQueue(e, "Deleted the url <%s>".formatted(randomUrl.url()));

    }


}
