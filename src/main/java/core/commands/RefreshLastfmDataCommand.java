package core.commands;

import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.UserInfo;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

public class RefreshLastfmDataCommand extends ConcurrentCommand<ChuuDataParams> {

    public RefreshLastfmDataCommand(ChuuService dao) {
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
        return "Use it when you change your lastfm profile pic so the bot updates its cached version";
    }

    @Override
    public List<String> getAliases() {
        return List.of("refresh");
    }

    @Override
    public String getName() {
        return "refresh";
    }

    @Override
    void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {
        UserInfo userInfo = lastFM.getUserInfo(List.of(params.getLastFMData().getName())).get(0);
        getService().insertUserInfo(userInfo);
        sendMessageQueue(e, "Sucessfully updated your profile data");
    }
}

