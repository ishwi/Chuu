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
import core.otherlisteners.Reactionary;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.TimeFrameParameters;
import core.parsers.utils.CustomTimeFrame;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;

import javax.validation.constraints.NotNull;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class YearDistributionCommand extends ConcurrentCommand<TimeFrameParameters> {


    public YearDistributionCommand(ChuuService dao) {
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
    protected void onCommand(Context e, @NotNull TimeFrameParameters params) throws LastFmException {
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
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, user.getDiscordId());
        List<String> lines = counts.entrySet().stream().sorted(Map.Entry.comparingByValue((Comparator.reverseOrder()))).map(t ->
                ". **%d**: %d %s%n".formatted(t.getKey().getValue(), t.getValue(), CommandUtil.singlePlural(t.getValue(), "album", "albums"))
        ).toList();

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < lines.size() && i < 20; i++) {
            String s = lines.get(i);
            a.append(i + 1).append(s);
        }

        var embedBuilder = new ChuuEmbedBuilder()
                .setDescription(a)
                .setAuthor(String.format("%s's years%s", uInfo.getUsername(), params.getTime().getDisplayString()), CommandUtil.getLastFmUser(params.getLastFMData().getName()), uInfo.getUrlImage())
                .setColor(ColorService.computeColor(e))
                .setFooter("%s has albums from %d different %s".formatted(CommandUtil.markdownLessString(uInfo.getUsername()), counts.size(), CommandUtil.singlePlural(counts.size(), "year", "years")), null);

        e.sendMessage(embedBuilder.build()).queue(m ->
                new Reactionary<>(lines, m, 20, embedBuilder));
    }


}
