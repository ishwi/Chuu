package core.commands.crowns;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.Parser;
import core.parsers.TwoUsersParser;
import core.parsers.params.NumberParameters;
import core.parsers.params.TwoUsersParamaters;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.StolenCrown;
import dao.entities.StolenCrownWrapper;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static core.parsers.NumberParser.generateThresholdParser;

public class CrownsStolenCommand extends ConcurrentCommand<NumberParameters<TwoUsersParamaters>> {
    public CrownsStolenCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<NumberParameters<TwoUsersParamaters>> initParser() {
        return generateThresholdParser(new TwoUsersParser(db));

    }

    @Override
    public String slashName() {
        return "stolen";
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
    protected void onCommand(Context e, @NotNull NumberParameters<TwoUsersParamaters> params) {


        TwoUsersParamaters innerParams = params.getInnerParams();
        long ogDiscordID = innerParams.getFirstUser().getDiscordId();
        String ogLastFmId = innerParams.getFirstUser().getName();
        long secondDiscordId = innerParams.getSecondUser().getDiscordId();
        String secondlastFmId = innerParams.getSecondUser().getName();

        if (ogLastFmId.equals(secondlastFmId) || ogDiscordID == secondDiscordId) {
            sendMessageQueue(e, "Sis, dont use the same person twice");
            return;
        }

        Long threshold = params.getExtraParam();
        long idLong = innerParams.getE().getGuild().getIdLong();

        if (threshold == null) {
            threshold = (long) db.getGuildCrownThreshold(idLong);
        }
        StolenCrownWrapper resultWrapper = db
                .getCrownsStolenBy(ogLastFmId, secondlastFmId, e.getGuild().getIdLong(), Math.toIntExact(threshold));

        int rows = resultWrapper.getList().size();

        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, ogDiscordID);
        String userName = userInformation.username();

        DiscordUserDisplay userInformation2 = CommandUtil.getUserInfoConsideringGuildOrNot(e, secondDiscordId);
        String userName2 = userInformation2.username();
        String userUrl2 = userInformation2.urlImage();
        if (rows == 0) {
            sendMessageQueue(e, userName2 + " hasn't stolen anything from " + userName);
            return;
        }
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setThumbnail(e.getGuild().getIconUrl());
        StringBuilder a = new StringBuilder();

        List<StolenCrown> list = resultWrapper.getList();
        for (int i = 0; i < 10 && i < rows; i++) {
            StolenCrown g = list.get(i);
            a.append(i + 1).append(g.toString());

        }

        // Footer doesnt allow markdown characters
        embedBuilder.setDescription(a).setTitle(userName + "'s stolen crowns by " + userName2, CommandUtil
                        .getLastFmUser(ogLastFmId))
                .setThumbnail(userUrl2)
                .setFooter(CommandUtil.unescapedUser(userName2, resultWrapper.getQuriedId(), e) + " has stolen " + rows + " crowns!\n", null);
        e.sendMessage(embedBuilder.build()).queue(m ->
                new Reactionary<>(resultWrapper.getList(), m, embedBuilder));

    }


}
