package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.UserInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class ScrobblesCommand extends ConcurrentCommand<ChuuDataParams> {
    public ScrobblesCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
        return new OnlyUsernameParser(getService());
    }

    @Override
    public String getDescription() {
        return "THe total number of scrobbles you have";
    }

    @Override
    public List<String> getAliases() {
        return List.of("scrobbles", "s");
    }

    @Override
    public String getName() {
        return "Scrobbles";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ChuuDataParams parse = parser.parse(e);
        // No need for null check i think?
        LastFMData lastFMData = parse.getLastFMData();
        List<UserInfo> userInfoes = lastFM.getUserInfo(Collections.singletonList(lastFMData.getName()));
        UserInfo ui = userInfoes.get(0);
        String userString = getUserString(e, lastFMData.getDiscordId());
        sendMessageQueue(e, String.format("%s has a total of  %d scrobbles", userString, ui.getPlayCount()));
    }
}
