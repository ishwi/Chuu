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
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.util.pie.IPieableLanguage;
import core.imagerenderer.util.pie.IPieableMap;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.CommandParameters;
import core.parsers.params.TimeFrameParameters;
import core.parsers.utils.CustomTimeFrame;
import core.parsers.utils.Optionals;
import core.util.ServiceView;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.ToLongFunction;

public class LanguageCommand extends ConcurrentCommand<TimeFrameParameters> {
    private final MusicBrainzService mb;
    private final IPieableMap<Language, Long, CommandParameters> iPie;

    public LanguageCommand(ServiceView dao) {
        super(dao);
        this.mb = MusicBrainzServiceSingleton.getInstance();
        iPie = new IPieableLanguage(getParser());
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<TimeFrameParameters> initParser() {
        TimerFrameParser timerFrameParser = new TimerFrameParser(db, TimeFrameEnum.ALL);
        timerFrameParser.addOptional(Optionals.PIE.opt);
        return timerFrameParser;
    }

    @Override
    public String getDescription() {
        return "List of the languages you listen your music";
    }

    @Override
    public List<String> getAliases() {
        return List.of("languages", "language", "lang");
    }

    @Override
    public String getName() {
        return "Languages";
    }

    @Override
    public void onCommand(Context e, @Nonnull TimeFrameParameters params) throws LastFmException {


        BlockingQueue<UrlCapsule> queue = new ArrayBlockingQueue<>(3000);
        LastFMData user = params.getLastFMData();
        String name = user.getName();
        long discordId = user.getDiscordId();
        List<AlbumInfo> albumInfos;
        if (params.getTime().equals(TimeFrameEnum.ALL)) {
            albumInfos = db.getUserAlbums(name).stream().filter(u -> u.getAlbumMbid() != null && !u.getAlbumMbid().isEmpty()).map(x ->
                    new AlbumInfo(x.getAlbumMbid(), x.getAlbum(), x.getArtist())).toList();

        } else {
            lastFM.getChart(user, CustomTimeFrame.ofTimeFrameEnum(params.getTime()), 3000, 1, TopEntity.ALBUM, ChartUtil.getParser(CustomTimeFrame.ofTimeFrameEnum(params.getTime()), TopEntity.ALBUM, ChartParameters.toListParams(), lastFM, user), queue);

            albumInfos = queue.stream().filter(x -> x.getMbid() != null && !x.getMbid().isBlank()).map(x -> new AlbumInfo(x.getMbid(), null, null)).toList();
        }
        Map<Language, Long> languageCountByMbid = this.mb.getLanguageCountByMbid(albumInfos);

        DiscordUserDisplay userInformation = CommandUtil.getUserInfoEscaped(e, discordId);
        String userName = userInformation.username();
        String userUrl = userInformation.urlImage();
        String usableTime = params.getTime().getDisplayString();
        if (languageCountByMbid.isEmpty()) {
            sendMessageQueue(e, "Couldn't find any language in " + userName + " albums" + usableTime);
            return;
        }
        if (params.hasOptional("pie")) {
            doPie(languageCountByMbid, params);
            return;
        }

        List<String> stringedList = languageCountByMbid.entrySet().stream().sorted(Comparator.comparingLong((ToLongFunction<Map.Entry<Language, Long>>) Map.Entry::getValue).reversed()).map((t) ->
                        String.format(". **%s** - %s %s\n", CommandUtil.escapeMarkdown(t.getKey().getName()), t.getValue().toString(), CommandUtil.singlePlural(Math.toIntExact(t.getValue()), "album", "albums")))
                .toList();

        String title = userName + "'s most common languages" + params.getTime().getDisplayString();
        long count = languageCountByMbid.keySet().size();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setThumbnail(userUrl)
                .setFooter(String.format("%s has %d%s%s", CommandUtil.unescapedUser(userName, discordId, e), count, count == 1 ? " language" : " languages", usableTime), null)
                .setTitle(title);

        new PaginatorBuilder<>(e, embedBuilder, stringedList).pageSize(15).build().queue();
    }

    void doPie(Map<Language, Long> map, TimeFrameParameters parameters) {
        PieChart pieChart = this.iPie.doPie(parameters, map);
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(parameters.getE(), parameters.getLastFMData().getDiscordId());

        pieChart.setTitle(uInfo.username() + "'s languages" + parameters.getTime().getDisplayString());
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        pieChart.paint(g, 1000, 750);

        sendImage(bufferedImage, parameters.getE());
    }
}
