package core.commands.config;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.services.UserInfoService;
import dao.ServiceView;

import javax.validation.constraints.NotNull;
import java.util.List;

public class RefreshLastfmDataCommand extends ConcurrentCommand<ChuuDataParams> {

    public RefreshLastfmDataCommand(ServiceView dao) {
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
    protected void onCommand(Context e, @NotNull ChuuDataParams params) throws LastFmException {
        new UserInfoService(db).refreshUserInfo(params.getLastFMData());
        sendMessageQueue(e, "Successfully updated %s's profile data".formatted(getUserString(e, params.getLastFMData().getDiscordId(), params.getLastFMData().getName())));
    }
}

