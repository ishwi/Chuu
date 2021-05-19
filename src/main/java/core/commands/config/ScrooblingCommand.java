package core.commands.config;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;
import dao.entities.LastFMData;

import javax.validation.constraints.NotNull;
import java.util.List;

public class ScrooblingCommand extends ConcurrentCommand<ChuuDataParams> {
    public ScrooblingCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
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
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {
        LastFMData lastFMData = params.getLastFMData();
        boolean scrobbling = lastFMData.isScrobbling();
        char messagePrefix = CommandUtil.getMessagePrefix(e);

        if (lastFMData.getSession() == null) {
            sendMessageQueue(e, "You have not authorized " + params.getE().getJDA().getSelfUser().getName() + " to scrobble!\n" +
                                "do `" + messagePrefix + "login` to enable it.");
            return;
        }
        if (params.hasOptional("enable")) {
            db.changeScrobbling(params.getLastFMData().getDiscordId(), true);
            sendMessageQueue(e, "**Enabled** the scrobbling features for your account");
            return;
        } else if (params.hasOptional("disable")) {
            db.changeScrobbling(params.getLastFMData().getDiscordId(), false);
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
