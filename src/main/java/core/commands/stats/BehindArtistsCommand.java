package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.TwoUsersParser;
import core.parsers.params.NumberParameters;
import core.parsers.params.TwoUsersParamaters;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.StolenCrownWrapper;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class BehindArtistsCommand extends ConcurrentCommand<NumberParameters<TwoUsersParamaters>> {
    public BehindArtistsCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<TwoUsersParamaters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to filter artist below that number ";
        return new NumberParser<>(new TwoUsersParser(db),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true, "filter");
    }

    @Override
    public String getDescription() {
        return ("List of artists that you have less plays than the second user");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("behind");
    }

    @Override
    public String getName() {
        return "Behind list";
    }

    @Override
    protected void onCommand(Context e, @Nonnull NumberParameters<TwoUsersParamaters> params) {


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

        if (threshold == null) {
            threshold = 0L;
        }
        StolenCrownWrapper resultWrapper = db
                .getArtistsBehind(ogLastFmId, secondlastFmId, Math.toIntExact(threshold));

        int rows = resultWrapper.getList().size();

        DiscordUserDisplay userInformation = CommandUtil.getUserInfoEscaped(e, ogDiscordID);
        String userName = userInformation.username();

        DiscordUserDisplay userInformation2 = CommandUtil.getUserInfoEscaped(e, secondDiscordId);
        String userName2 = userInformation2.username();
        String userUrl2 = userInformation2.urlImage();
        if (rows == 0) {
            sendMessageQueue(e, userName2 + " doesn't have any artist with more plays than " + userName);
            return;
        }
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setThumbnail(e.getGuild().getIconUrl())
                .setTitle(userName + "'s artist behind " + userName2, CommandUtil.getLastFmUser(ogLastFmId))
                .setThumbnail(userUrl2)
                .setFooter(CommandUtil.unescapedUser(userName, resultWrapper.getQuriedId(), e) + " is behind in " + rows + " artists!\n", null);

        new PaginatorBuilder<>(e, embedBuilder, resultWrapper.getList()).build().queue();


    }
}
