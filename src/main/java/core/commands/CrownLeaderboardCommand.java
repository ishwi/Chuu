package core.commands;

import core.otherlisteners.Reactionary;
import dao.ChuuService;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class CrownLeaderboardCommand extends ListCommand<LbEntry> {
    String entryName = "Crowns";

    public CrownLeaderboardCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;

    }

    @Override
    public String getDescription() {
        return ("Users of a server ranked by crowns");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("crownslb");
    }

    @Override
    public String getName() {
        return "Crown Leaderboard";
    }


    @Override
    public List<LbEntry> getList(String[] message, MessageReceivedEvent e) {
        return getService().getGuildCrownLb(e.getGuild().getIdLong());
    }

    @Override
    public void printList(List<LbEntry> list, MessageReceivedEvent e) {
        list.forEach(cl -> cl.setDiscordName(getUserString(e, cl.getDiscordId(), cl.getLastFmId())));
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(e.getGuild().getIconUrl());
        StringBuilder a = new StringBuilder();

        if (list.isEmpty()) {
            sendMessageQueue(e, "This guild has no registered users:(");
            return;
        }

        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i).toString());
        }
        embedBuilder.setDescription(a).setTitle(CommandUtil.cleanMarkdownCharacter(e.getGuild().getName()) + "'s " + entryName + " leadearboard")
                .setThumbnail(e.getGuild().getIconUrl())
                .setFooter(e.getGuild().getName() + " has " + list.size() + " registered users!\n", null);
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(message ->
                executor.execute(() -> new Reactionary<>(list, message, embedBuilder)));
    }


}
