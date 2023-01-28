package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.ui.UserCommandMarker;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.util.ServiceView;
import dao.entities.DiscordUserDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LastFmLinkCommand extends ConcurrentCommand<ChuuDataParams> implements UserCommandMarker {
    public LastFmLinkCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "Link to a user's last.fm page";
    }

    @Override
    public List<String> getAliases() {
        return List.of("link", "lfm");
    }

    @Override
    public String getName() {
        return "Last.fm User Page";
    }

    @Override
    public void onCommand(Context e, @NotNull ChuuDataParams params) {


        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoEscaped(e, params.getLastFMData().getDiscordId());

        sendMessageQueue(e, userInfoConsideringGuildOrNot.username() + "'s Last.fm page is: " + CommandUtil.getLastFmUser(params.getLastFMData().getName()));
    }
}
