package core.commands.config;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class ScrooblingCommand extends ConcurrentCommand<ChuuDataParams> {
    public ScrooblingCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(getService());
    }

    @Override
    public String getDescription() {
        return "Checks your scrobbling status within the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("scrobbling", "scrobble");
    }

    @Override
    public String getName() {
        return "Scrobbling";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) {
        LastFMData lastFMData = params.getLastFMData();
        boolean scrobbling = lastFMData.isScrobbling();
        char messagePrefix = CommandUtil.getMessagePrefix(e);

        if (lastFMData.getSession() == null) {
            sendMessageQueue(e, "You have not authorized " + params.getE().getJDA().getSelfUser().getName() + " to scrobble!\n" +
                    "do `" + messagePrefix + "login` to enable it.");
            return;
        }
        if (params.hasOptional("enable")) {
            getService().changeScrobbling(params.getLastFMData().getDiscordId(), true);
            sendMessageQueue(e, "**Enabled** the scrobbling features for your account");
            return;
        } else if (params.hasOptional("disable")) {
            getService().changeScrobbling(params.getLastFMData().getDiscordId(), false);
            sendMessageQueue(e, "**Disabled** the scrobbling features for your account");
            return;
        }
        if (scrobbling) {
            sendMessageQueue(e, "You currently can scrobble anything I play on a voice channel!\n `" + messagePrefix + "scrobble --disable` to disable this option ");
        } else {
            sendMessageQueue(e, "You have the scrobbling feature disabled.\n `" + messagePrefix + "scrobble --enable` to enable this option ");
        }
    }


}
