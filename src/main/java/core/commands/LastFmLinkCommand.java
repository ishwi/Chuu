package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class LastFmLinkCommand extends ConcurrentCommand<ChuuDataParams> {
    public LastFmLinkCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ChuuDataParams parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        DiscordUserDisplay userInfoConsideringGuildOrNot = CommandUtil.getUserInfoConsideringGuildOrNot(e, parse.getLastFMData().getDiscordId());

        sendMessageQueue(e, userInfoConsideringGuildOrNot.getUsername() + "'s Last.fm page is: " + CommandUtil.getLastFmUser(parse.getLastFMData().getName()));
    }
}
