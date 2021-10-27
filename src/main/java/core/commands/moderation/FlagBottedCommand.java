package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.UserModerationParser;
import core.parsers.params.ChuuDataParams;
import core.parsers.utils.OptionalEntity;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;

import javax.annotation.Nonnull;
import java.util.List;

public class FlagBottedCommand extends ConcurrentCommand<ChuuDataParams> {
    public FlagBottedCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new UserModerationParser(db)
                .addOptional(new OptionalEntity("unflag", "unflag a flagged account"))
                .addOptional(new OptionalEntity("check", "check a flagged account"));
    }

    @Override
    public String getDescription() {
        return "Flags users as a botted account";
    }

    @Override
    public List<String> getAliases() {
        return List.of("botted");
    }

    @Override
    public String getName() {
        return "Flag botters";
    }

    @Override
    public void onCommand(Context e, @Nonnull ChuuDataParams params) throws InstanceNotFoundException {
        LastFMData lastFMData = db.findLastFMData(e.getAuthor().getIdLong());
        LastFMData botter = params.getLastFMData();
        if (lastFMData.getRole() != Role.ADMIN) {
            boolean isFlagged = db.isFlagged(botter.getName());
            sendMessageQueue(e, "**%s** botted status: **%s**".formatted(botter.getName(), isFlagged));
            return;
        }
        if (params.hasOptional("unflag")) {
            db.unflagAsBotted(botter.getName());
            sendMessageQueue(e, "Unflagged %s as a botter".formatted(botter.getName()));
        } else if (params.hasOptional("check")) {
            boolean isFlagged = db.isFlagged(botter.getName());
            sendMessageQueue(e, "**%s** botted status: **%s**".formatted(botter.getName(), isFlagged));
        } else {
            db.flagAsBotted(botter.getName());
            sendMessageQueue(e, "Flagged %s as a botter".formatted(botter.getName()));
        }
    }
}
