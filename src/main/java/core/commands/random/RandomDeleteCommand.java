package core.commands.random;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.RandomAlbumParser;
import core.parsers.params.RandomUrlParameters;
import core.util.ServiceView;
import dao.entities.LastFMData;
import dao.entities.RandomUrlDetails;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RandomDeleteCommand extends ConcurrentCommand<RandomUrlParameters> {
    public RandomDeleteCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<RandomUrlParameters> initParser() {
        return new RandomAlbumParser(db);
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
    public void onCommand(Context e, @NotNull RandomUrlParameters params) throws InstanceNotFoundException {


        String url = params.getUrl();
        if (url == null || url.isBlank()) {
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
        db.deleteRandomUrl(url);
        sendMessageQueue(e, "Deleted the url <%s>".formatted(randomUrl.url()));

    }


}
