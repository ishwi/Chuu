package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.Parser;
import core.parsers.TwoUsersParser;
import core.parsers.params.TwoUsersParamaters;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.StolenCrown;
import dao.entities.StolenCrownWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class CrownsStolenCommand extends ConcurrentCommand<TwoUsersParamaters> {
    public CrownsStolenCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    public Parser<TwoUsersParamaters> getParser() {
        return new TwoUsersParser(getService());
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
        TwoUsersParamaters params = parser.parse(e);
        if (params == null)
            return;

        long ogDiscordID = params.getFirstUser().getDiscordId();
        String ogLastFmId = params.getFirstUser().getName();
        long secondDiscordId = params.getSecondUser().getDiscordId();
        String secondlastFmId = params.getSecondUser().getName();

        if (ogLastFmId.equals(secondlastFmId) || ogDiscordID == secondDiscordId) {
            sendMessageQueue(e, "Sis, dont use the same person twice");
            return;
        }

        StolenCrownWrapper resultWrapper = getService()
                .getCrownsStolenBy(ogLastFmId, secondlastFmId, e.getGuild().getIdLong());

        int rows = resultWrapper.getList().size();

        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, ogDiscordID);
        String userName = userInformation.getUsername();

        DiscordUserDisplay userInformation2 = CommandUtil.getUserInfoConsideringGuildOrNot(e, secondDiscordId);
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
                new Reactionary<>(resultWrapper.getList(), m, embedBuilder));

    }


}
