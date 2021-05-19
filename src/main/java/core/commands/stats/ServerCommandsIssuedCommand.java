package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.Memoized;
import dao.entities.UserCount;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
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
    public String getName() {
        return "Server spammers";
    }


    @Override
    protected void onCommand(Context e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        List<UserCount> userCommands = db.getServerCommandsLb(e.getGuild().getIdLong());

        if (userCommands.isEmpty()) {
            sendMessageQueue(e, e.getGuild().getName() + " doesn't have any user that have ran any command!");
            return;
        }

        Function<UserCount, String> toMemoize = (userListened) -> ". [" + CommandUtil.getUserInfoConsideringGuildOrNot(e, userListened.discordId()).getUsername() + "]" +
                                                                  "(" + PrivacyUtils.getLastFmUser(userListened.lastfmId()) + ")" +
                                                                  ": " + userListened.count() + " " + CommandUtil.singlePlural(userListened.count(), "command", "commands") + "\n";

        List<Memoized<UserCount, String>> strings = userCommands.stream().map(t -> new Memoized<>(t, toMemoize)).toList();

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < strings.size(); i++) {
            a.append(i + 1).append(strings.get(i));
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setDescription(a)
                .setAuthor(e.getGuild().getName() + "'s spammers", null, e.getGuild().getIconUrl());
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(strings, message1, embedBuilder));
    }

}
