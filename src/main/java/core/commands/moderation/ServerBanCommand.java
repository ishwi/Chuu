package core.commands.moderation;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;

import javax.validation.constraints.NotNull;
import java.util.List;

public class ServerBanCommand extends ConcurrentCommand<ChuuDataParams> {
    public ServerBanCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
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
        return "Lets server administrators block/unblock one user from this server leaderboard";
    }

    @Override
    public List<String> getAliases() {
        return List.of("serverblock", "serverunblock", "serverban", "serverunban");
    }

    @Override
    public String getName() {
        return "Server Block";
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) {

        if (CommandUtil.notEnoughPerms(e)) {
            sendMessageQueue(e, CommandUtil.notEnoughPermsTemplate() + "block one person from crowns/leaderboards");
            return;
        }

        long discordId = params.getLastFMData().getDiscordId();
        if (discordId == e.getAuthor().getIdLong()) {
            sendMessageQueue(e, "You can't block/unblock yourself.");
            return;
        }
        String contentRaw = parser.getAlias(e);
        String user = getUserString(e, discordId);
        String server = e.getGuild().getName();
        if (contentRaw.startsWith("serverblock") || contentRaw.startsWith("serverban")) {
            db.serverBlock(discordId, e.getGuild().getIdLong());
            sendMessageQueue(e, String.format("Successfully blocked %s from %s's leaderboards", user, server));

        } else {
            db.serverUnblock(discordId, e.getGuild().getIdLong());
            sendMessageQueue(e, String.format("Successfully unblocked %s from %s's leaderboards", user, server));
        }
    }
}
