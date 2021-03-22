package core.commands.moderation;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.UserModerationParser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class FlagBottedCommand extends ConcurrentCommand<ChuuDataParams> {
    public FlagBottedCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new UserModerationParser(db);
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
    protected void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws InstanceNotFoundException {
        LastFMData lastFMData = db.findLastFMData(e.getAuthor().getIdLong());
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Only bot admins can flag people as bots!");
            return;
        }
        LastFMData botter = params.getLastFMData();
        db.flagAsBotted(botter.getDiscordId());
        sendMessageQueue(e, "Flagged %s as a botter".formatted(botter.getName()));
    }
}
