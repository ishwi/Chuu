package core.commands;

import core.commands.utils.PrivacyUtils;
import core.otherlisteners.Reactionary;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.stream.Collectors;

public abstract class LeaderboardCommand<T extends CommandParameters> extends ListCommand<LbEntry, T> {

    public LeaderboardCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    public abstract String getEntryName(T params);

    @Override
    public void printList(List<LbEntry> list, T params) {
        MessageReceivedEvent e = params.getE();
        list.forEach(cl -> cl.setDiscordName(getUserString(e, cl.getDiscordId(), cl.getLastFmId())));
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(e.getGuild().getIconUrl());
        StringBuilder a = new StringBuilder();

        if (list.isEmpty()) {
            sendMessageQueue(e, "This guild has no registered users:(");
            return;
        }

        List<String> strings = list.stream().map(PrivacyUtils::toString).collect(Collectors.toUnmodifiableList());
        for (int i = 0; i < 10 && i < strings.size(); i++) {
            a.append(i + 1).append(strings.get(i));
        }

        embedBuilder.setDescription(a).setTitle(CommandUtil.cleanMarkdownCharacter(e.getGuild().getName()) + "'s " + getEntryName(params) + " leaderboard")
                .setThumbnail(e.getGuild().getIconUrl())
                .setFooter(e.getGuild().getName() + " has " + list.size() + " registered users!\n", null);

        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build()).queue(message ->
                new Reactionary<>(strings, message, embedBuilder));
    }
}
