package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class LastFmLinkCommand extends ConcurrentCommand<ChuuDataParams> {
    public LastFmLinkCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(getService());
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
    protected void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {


        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoConsideringGuildOrNot(e, params.getLastFMData().getDiscordId());

        sendMessageQueue(e, userInfoConsideringGuildOrNot.getUsername() + "'s Last.fm page is: " + CommandUtil.getLastFmUser(params.getLastFMData().getName()));
    }
}
