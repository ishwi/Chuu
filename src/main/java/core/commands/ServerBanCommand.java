package core.commands;

import core.commands.ConcurrentCommand;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ServerBanCommand extends ConcurrentCommand<ChuuDataParams> {
    public ServerBanCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
        return new OnlyUsernameParser(getService());
    }

    @Override
    public String getDescription() {
        return "Lets server administrators to block/unblock one user from this server leaderboard";
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ChuuDataParams parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        if (e.getMember() != null && !e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            sendMessageQueue(e, "Only server administrators can block one person from crowns/leaderboards.");
            return;
        }
        long discordId = parse.getLastFMData().getDiscordId();
        if (discordId == e.getAuthor().getIdLong()) {
            sendMessageQueue(e, "You can't block/unblock yourself.");
            return;
        }
        String contentRaw = e.getMessage().getContentRaw().substring(1).toLowerCase();
        String user = getUserString(e, discordId);
        String server = e.getGuild().getName();
        if (contentRaw.startsWith("serverblock") || contentRaw.startsWith("serverban")) {
            getService().serverBlock(discordId, e.getGuild().getIdLong());
            sendMessageQueue(e, String.format("Successfully blocked %s from %s's leaderboards", user, server));

        } else {
            getService().serverUnblock(discordId, e.getGuild().getIdLong());
            sendMessageQueue(e, String.format("Successfully unblocked %s from %s's leaderboards", user, server));
        }
    }
}
