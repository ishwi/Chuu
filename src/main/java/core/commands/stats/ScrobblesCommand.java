package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.UserInfo;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class ScrobblesCommand extends ConcurrentCommand<ChuuDataParams> {
    public ScrobblesCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
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
    protected void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {

        LastFMData lastFMData = params.getLastFMData();
        List<UserInfo> userInfoes = lastFM.getUserInfo(Collections.singletonList(lastFMData.getName()), lastFMData);
        UserInfo ui = userInfoes.get(0);
        String userString = getUserString(e, lastFMData.getDiscordId());
        sendMessageQueue(e, String.format("%s has a total of %d scrobbles", userString, ui.getPlayCount()));
    }
}
