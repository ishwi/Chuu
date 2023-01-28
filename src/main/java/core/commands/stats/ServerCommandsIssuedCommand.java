package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import dao.entities.UserCount;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class ServerCommandsIssuedCommand extends ConcurrentCommand<CommandParameters> {

    public ServerCommandsIssuedCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_LEADERBOARDS;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "People that have run the most commands in a server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("commandslb", "commandlb", "ranlb", "commandsleaderboard", "ranleaderboard");
    }

    @Override
    public String slashName() {
        return "commands";
    }

    @Override
    public String getName() {
        return "Server spammers";
    }


    @Override
    public void onCommand(Context e, @NotNull CommandParameters params) {
        List<UserCount> userCommands = db.getServerCommandsLb(e.getGuild().getIdLong());

        if (userCommands.isEmpty()) {
            sendMessageQueue(e, e.getGuild().getName() + " doesn't have any user that have ran any command!");
            return;
        }

        Function<UserCount, String> toMemoize = (userListened) -> ". [" + CommandUtil.getUserInfoEscaped(e, userListened.discordId()).username() + "]" +
                                                                  "(" + PrivacyUtils.getLastFmUser(userListened.lastfmId()) + ")" +
                                                                  ": " + userListened.count() + " " + CommandUtil.singlePlural(userListened.count(), "command", "commands") + "\n";


        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(e.getGuild().getName() + "'s spammers", null, e.getGuild().getIconUrl());

        new PaginatorBuilder<>(e, embedBuilder, userCommands).memoized(toMemoize).build().queue();
    }

}
