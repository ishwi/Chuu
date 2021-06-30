package core.commands.abstracts;

import core.commands.Context;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.Reactionary;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.collections4.ListUtils;

import java.util.List;

public abstract class LeaderboardCommand<T extends CommandParameters, Y extends Number> extends ListCommand<LbEntry<Y>, T> {
    public LeaderboardCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, isLongRunningCommand);
        this.respondInPrivate = false;
    }

    public LeaderboardCommand(ServiceView dao) {
        this(dao, false);
    }

    public abstract String getEntryName(T params);

    @Override
    public void printList(List<LbEntry<Y>> list, T params) {
        Context e = params.getE();
        list.forEach(cl -> cl.setDiscordName(getUserString(e, cl.getDiscordId(), cl.getLastFmId())));

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
        StringBuilder a = new StringBuilder();

        if (list.isEmpty()) {
            sendMessageQueue(e, "This guild has no registered users:(");
            return;
        }

        List<String> strings = list.stream().map(PrivacyUtils::toString).toList();
        for (int i = 0; i < 10 && i < strings.size(); i++) {
            a.append(i + 1).append(strings.get(i));
        }
        if (strings.size() > 10) {
            embedBuilder.setFooter("%s has %d users with %s\n".formatted(e.getGuild().getName(), list.size(), getEntryName(params)), null);
        }
        embedBuilder.setDescription(a)
                .setAuthor(e.getGuild().getName() + "'s " + getEntryName(params) + " leaderboard", null, e.getGuild().getIconUrl());

        setFooter(embedBuilder, list, params);
        e.sendMessage(embedBuilder.build()).queue(message ->
                new Reactionary<>(strings, message, embedBuilder));
    }

    protected void setFooter(EmbedBuilder embedBuilder, List<LbEntry<Y>> list, T params) {
        Context e = params.getE();
        String text = "";
        if (list.size() > 10) {
            text = "%s has %d registered %s!\n".formatted(e.getGuild().getName(), list.size(), CommandUtil.singlePlural(list.size(), "user", "users"));
        }
        embedBuilder.setFooter(text + userStringFooter(embedBuilder, list, params), null);
    }

    protected String userStringFooter(EmbedBuilder embedBuilder, List<LbEntry<Y>> list, T params) {
        Context e = params.getE();
        long discordId = e.getAuthor().getIdLong();
        int i = ListUtils.indexOf(list, (w -> w.getDiscordId() == discordId));
        String text = "";
        if (i > 10) {
            String user = getUserString(e, discordId);
            text = "%s is %d%s - %s".formatted(user, i + 1, CommandUtil.getRank(i + 1), ChuuEmbedBuilder.linkMatcher.matcher(list.get(i).toStringWildcard()).replaceAll("").replaceAll("^[.: -]*", "").replaceAll("\\*", ""));
        }
        return text;
    }
}
