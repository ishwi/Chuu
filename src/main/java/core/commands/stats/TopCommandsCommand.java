package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.abstracts.MyCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;
import dao.entities.CommandUsage;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TopCommandsCommand extends ConcurrentCommand<ChuuDataParams> {
    private static Map<String, String> aliasMap;

    public TopCommandsCommand(ServiceView dao) {
        super(dao);
    }

    private static Map<String, String> getMap(JDA jda) {
        if (aliasMap == null) {
            aliasMap = jda.getRegisteredListeners().stream().filter(obj -> obj instanceof MyCommand<?>).map
                    (t -> (MyCommand<?>) t).collect(Collectors.toMap(MyCommand::getName, t -> t.getAliases().get(0)));
        }
        return aliasMap;

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "Your most used commands";
    }

    @Override
    public List<String> getAliases() {
        return List.of("commands", "ran");
    }

    @Override
    public String getName() {
        return "Commands used";
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {
        LastFMData lastFMData = params.getLastFMData();
        List<CommandUsage> userCommands = db.getUserCommands(params.getLastFMData().getDiscordId());

        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());

        if (userCommands.isEmpty()) {
            sendMessageQueue(e, uInfo.getUsername() + " hasn't run any command.");
            return;
        }

        List<String> strings = userCommands.stream().map(t -> mapString(t, e.getJDA())).filter(Objects::nonNull).toList();
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < strings.size(); i++) {
            a.append(i + 1).append(strings.get(i));
        }


        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setDescription(a)
                .setAuthor(uInfo.getUsername() + "'s commands", PrivacyUtils.getLastFmUser(params.getLastFMData().getName()), uInfo.getUrlImage());
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(strings, message1, embedBuilder));
    }

    private @Nullable String mapString(CommandUsage command, JDA jda) {
        Map<String, String> map = getMap(jda);
        String alias = map.get(command.command());
        if (alias == null) {
            return null;
        }
        return ". **%s**: %s %s\n".formatted(alias, command.count(), CommandUtil.singlePlural(command.count(), "use", "uses"));
    }


}
