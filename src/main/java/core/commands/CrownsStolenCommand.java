package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.TwoUsersParser;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.StolenCrown;
import dao.entities.StolenCrownWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class CrownsStolenCommand extends ConcurrentCommand {
    public CrownsStolenCommand(ChuuService dao) {
        super(dao);
        parser = new TwoUsersParser(dao);
        this.respondInPrivate = false;

    }

    @Override
    public String getDescription() {
        return ("List of crowns you would have if the other would concedes their crowns");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("stolen");
    }

    @Override
    public String getName() {
        return "List of stolen crowns";
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] message;
        message = parser.parse(e);
        if (message == null)
            return;

        String ogLastFmId = message[0];
        String secondlastFmId = message[1];
        if (ogLastFmId.equals(secondlastFmId)) {
            sendMessageQueue(e, "Sis, dont use the same person twice");
            return;
        }

        StolenCrownWrapper resultWrapper = getService()
                .getCrownsStolenBy(ogLastFmId, secondlastFmId, e.getGuild().getIdLong());

        int rows = resultWrapper.getList().size();
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, resultWrapper.getOgId());
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();

        DiscordUserDisplay userInformation2 = CommandUtil.getUserInfoConsideringGuildOrNot(e, resultWrapper.getQuriedId());
        String userName2 = userInformation2.getUsername();
        String userUrl2 = userInformation2.getUrlImage();
        if (rows == 0) {
            sendMessageQueue(e, userName2 + " hasn't stolen anything from " + userName);
            return;
        }
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(e.getGuild().getIconUrl());
        StringBuilder a = new StringBuilder();

        List<StolenCrown> list = resultWrapper.getList();
        for (int i = 0; i < 10 && i < rows; i++) {
            StolenCrown g = list.get(i);
            a.append(i + 1).append(g.toString());

        }

        // Footer doesnt allow markdown characters
        embedBuilder.setDescription(a).setTitle(userName + "'s Top Crowns stolen by " + userName2, CommandUtil
                .getLastFmUser(ogLastFmId))
                .setThumbnail(userUrl2)
                .setFooter(CommandUtil.markdownLessUserString(userName2, resultWrapper.getQuriedId(), e) + " has stolen " + rows + " crowns!\n", null);
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(m ->
                executor.execute(() -> new Reactionary<>(resultWrapper.getList(), m, embedBuilder)));

    }


}
