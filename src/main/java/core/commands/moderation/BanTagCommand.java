package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;

import javax.annotation.Nonnull;
import java.util.List;

public class BanTagCommand extends ConcurrentCommand<CommandParameters> {
    public BanTagCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Bans a tag from the bot system";
    }

    @Override
    public List<String> getAliases() {
        return List.of("bantag");
    }

    @Override
    public String getName() {
        return "Ban tag";
    }

    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) throws InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(idLong);
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Only bot admins can delete tags");
            return;
        }
        String[] subMessage = parser.getSubMessage(e);
        String joined = String.join(" ", subMessage).trim();
        db.addBannedTag(joined, idLong);
        if (!joined.isBlank()) {
            sendMessageQueue(e, "Successfully banned the tag: " + joined);
            return;
        }
        sendMessageQueue(e, "Bruh.");

    }
}
