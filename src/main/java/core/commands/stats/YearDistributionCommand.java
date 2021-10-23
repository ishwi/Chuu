package core.commands.stats;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.TimeFrameParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ServiceView;
import dao.entities.AlbumInfo;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class YearDistributionCommand extends ConcurrentCommand<TimeFrameParameters> {


    public YearDistributionCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<TimeFrameParameters> initParser() {
        return new TimerFrameParser(db, TimeFrameEnum.ALL);

    }

    @Override
    public String getDescription() {
        return "Which year are your albums from?";
    }

    @Override
    public List<String> getAliases() {
        return List.of("years", "year");
    }

    @Override
    public String getName() {
        return "Album years";
    }

    @Override
    protected void onCommand(Context e, @Nonnull TimeFrameParameters params) throws LastFmException {
        LastFMData user = params.getLastFMData();
        Map<Year, Integer> counts;
        if (params.getTime() == TimeFrameEnum.ALL) {
            counts = db.getUserYears(user.getName());
        } else {
            CustomTimeFrame ctF = CustomTimeFrame.ofTimeFrameEnum(params.getTime());
            BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
            lastFM.getChart(user, ctF,
                    3000, 1, TopEntity.ALBUM, ChartUtil.getParser(ctF, TopEntity.ALBUM, ChartParameters.toListParams(), lastFM, user), queue);
            List<AlbumInfo> albums = queue.stream().map(t -> new AlbumInfo(t.getAlbumName(), t.getArtistName())).toList();
            counts = db.getUserYearsFromList(user.getName(), albums);
        }
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoEscaped(e, user.getDiscordId());
        List<String> lines = counts.entrySet().stream().sorted(Map.Entry.comparingByValue((Comparator.reverseOrder()))).map(t ->
                ". **%d**: %d %s%n".formatted(t.getKey().getValue(), t.getValue(), CommandUtil.singlePlural(t.getValue(), "album", "albums"))
        ).toList();


        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(String.format("%s's years%s", uInfo.username(), params.getTime().getDisplayString()), CommandUtil.getLastFmUser(params.getLastFMData().getName()), uInfo.urlImage())
                .setFooter("%s has albums from %d different %s".formatted(CommandUtil.stripEscapedMarkdown(uInfo.username()), counts.size(), CommandUtil.singlePlural(counts.size(), "year", "years")), null);

        new PaginatorBuilder<>(e, embedBuilder, lines).pageSize(20).build().queue();
    }


}
