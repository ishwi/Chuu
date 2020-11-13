package core.commands;

import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class BanTagCommand extends ConcurrentCommand<CommandParameters> {
    public BanTagCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
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
    void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        LastFMData lastFMData = getService().findLastFMData(idLong);
        if (lastFMData.getRole() != Role.ADMIN) {
            sendMessageQueue(e, "Only bot admins can delete tags");
            return;
        }
        String[] subMessage = parser.getSubMessage(e.getMessage());
        String joined = String.join(" ", subMessage).trim();
        getService().addBannedTag(joined, idLong);
        if (!joined.isBlank()) {
            sendMessageQueue(e, "Succesfully banned the tag: " + joined);
            return;
        }
        sendMessageQueue(e, "Bruh.");

    }
}
