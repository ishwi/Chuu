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

import java.util.List;

public abstract class LeaderboardCommand<T extends CommandParameters> extends ListCommand<LbEntry, T> {
    public LeaderboardCommand(ServiceView dao, boolean isLongRunningCommand) {
        super(dao, isLongRunningCommand);
        this.respondInPrivate = false;
    }

    public LeaderboardCommand(ServiceView dao) {
        this(dao, false);
    }

    public abstract String getEntryName(T params);

    @Override
    public void printList(List<LbEntry> list, T params) {
        Context e = params.getE();
        list.forEach(cl -> cl.setDiscordName(getUserString(e, cl.getDiscordId(), cl.getLastFmId())));

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setThumbnail(e.getGuild().getIconUrl());
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
        embedBuilder.setDescription(a).setTitle(CommandUtil.escapeMarkdown(e.getGuild().getName()) + "'s " + getEntryName(params) + " leaderboard")
                .setThumbnail(e.getGuild().getIconUrl())
                .setFooter(e.getGuild().getName() + " has " + list.size() + " registered users!\n", null);

        e.sendMessage(embedBuilder.build()).queue(message ->
                new Reactionary<>(strings, message, embedBuilder));
    }
}
