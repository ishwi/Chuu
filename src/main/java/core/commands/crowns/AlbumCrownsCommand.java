package core.commands.crowns;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import core.util.ServiceView;
import dao.entities.AlbumPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static core.parsers.NumberParser.generateThresholdParser;

public class AlbumCrownsCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {
    public AlbumCrownsCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CROWNS;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {
        return generateThresholdParser(new OnlyUsernameParser(db));
    }


    @Override
    public String slashName() {
        return "albums";
    }

    @Override
    public String getDescription() {
        return ("Albums you are the top listener within a server");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("albumcrowns", "crownsalbum", "crownsal", "calb");
    }

    @Override
    public String getName() {
        return "Album crowns";
    }

    @Override
    public void onCommand(Context e, @NotNull NumberParameters<ChuuDataParams> params) {

        ChuuDataParams innerParams = params.getInnerParams();
        DiscordUserDisplay userInfo = CommandUtil.getUserInfoEscaped(e, innerParams.getLastFMData().getDiscordId());
        String name = userInfo.username();
        String url = userInfo.urlImage();
        Long threshold = params.getExtraParam();
        long idLong = innerParams.getE().getGuild().getIdLong();

        if (threshold == null) {
            threshold = (long) db.getGuildCrownThreshold(idLong);
        }
        UniqueWrapper<AlbumPlays> uniqueDataUniqueWrapper = db
                .getUserAlbumCrowns(innerParams.getLastFMData().getName(), e.getGuild().getIdLong(), Math.toIntExact(threshold));
        List<AlbumPlays> resultWrapper = uniqueDataUniqueWrapper.uniqueData();

        int rows = resultWrapper.size();
        if (rows == 0) {
            sendMessageQueue(e, name + " doesn't have any album crown :'(");
            return;
        }

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle(String.format("%s's album crowns", name), CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.lastFmId()))
                .setFooter(String.format("%s has %d album crowns!!%n", CommandUtil.unescapedUser(name, innerParams.getLastFMData().getDiscordId(), e), resultWrapper.size()), null)
                .setThumbnail(url);

        new PaginatorBuilder<>(e, embedBuilder, resultWrapper).build().queue();
    }

}



