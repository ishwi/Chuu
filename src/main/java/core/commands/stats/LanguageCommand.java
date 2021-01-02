package core.commands.stats;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.UrlCapsule;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.util.IPieableLanguage;
import core.imagerenderer.util.IPieableMap;
import core.otherlisteners.Reactionary;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.CommandParameters;
import core.parsers.params.TimeFrameParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public class LanguageCommand extends ConcurrentCommand<TimeFrameParameters> {
    private final MusicBrainzService mb;
    private final IPieableMap<Language, Long, CommandParameters> iPie;

    public LanguageCommand(ChuuService dao) {
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
        TimerFrameParser timerFrameParser = new TimerFrameParser(getService(), TimeFrameEnum.ALL);
        timerFrameParser.addOptional(new OptionalEntity("pie", "display it as a chart pie"));
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
    protected void onCommand(MessageReceivedEvent e, @NotNull TimeFrameParameters params) throws LastFmException, InstanceNotFoundException {


        BlockingQueue<UrlCapsule> queue = new ArrayBlockingQueue<>(3000);
        LastFMData user = params.getLastFMData();
        String name = user.getName();
        Long discordId = user.getDiscordId();
        List<AlbumInfo> albumInfos;
        if (params.getTime().equals(TimeFrameEnum.ALL)) {
            albumInfos = getService().getUserAlbumByMbid(name).stream().filter(u -> u.getAlbumMbid() != null && !u.getAlbumMbid().isEmpty()).map(x ->
                    new AlbumInfo(x.getAlbumMbid(), x.getAlbum(), x.getArtist())).collect(Collectors.toList());

        } else {
            lastFM.getChart(user, CustomTimeFrame.ofTimeFrameEnum(params.getTime()), 3000, 1, TopEntity.ALBUM, ChartUtil.getParser(CustomTimeFrame.ofTimeFrameEnum(params.getTime()), TopEntity.ALBUM, ChartParameters.toListParams(), lastFM, user), queue);

            albumInfos = queue.stream().filter(x -> x.getMbid() != null && !x.getMbid().isBlank()).map(x -> new AlbumInfo(x.getMbid(), null, null)).collect(Collectors.toList());
        }
        Map<Language, Long> languageCountByMbid = this.mb.getLanguageCountByMbid(albumInfos);

        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();
        String usableTime = params.getTime().getDisplayString();
        if (languageCountByMbid.isEmpty()) {
            sendMessageQueue(e, "Couldn't find any language in " + userName + " albums" + usableTime);
            return;
        }
        StringBuilder a = new StringBuilder();
        if (params.hasOptional("pie")) {
            doPie(languageCountByMbid, params);
            return;
        }

        List<String> stringedList = languageCountByMbid.entrySet().stream().sorted(Comparator.comparingLong((ToLongFunction<Map.Entry<Language, Long>>) Map.Entry::getValue).reversed()).map((t) ->
                String.format(". **%s** - %s %s\n", CommandUtil.cleanMarkdownCharacter(t.getKey().getName()), t.getValue().toString(), CommandUtil.singlePlural(Math.toIntExact(t.getValue()), "album", "albums")))
                .collect(Collectors.toList());

        for (int i = 0; i < 15 && i < stringedList.size(); i++) {
            a.append(i + 1).append(stringedList.get(i));
        }

        String title = userName + "'s most common languages" + params.getTime().getDisplayString();
        long count = languageCountByMbid.keySet().size();
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(userUrl)
                .setFooter(String.format("%s has %d%s%s", CommandUtil.markdownLessUserString(userName, discordId, e), count, count == 1 ? " language" : " languages", usableTime), null)
                .setTitle(title)
                .setDescription(a);
        e.getChannel().sendMessage(embedBuilder.build()).queue(mes ->
                new Reactionary<>(stringedList, mes, 15, embedBuilder));
    }

    void doPie(Map<Language, Long> map, TimeFrameParameters parameters) {
        PieChart pieChart = this.iPie.doPie(parameters, map);
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(parameters.getE(), parameters.getLastFMData().getDiscordId());

        pieChart.setTitle(uInfo.getUsername() + "'s languages" + parameters.getTime().getDisplayString());
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        GraphicUtils.setQuality(g);
        pieChart.paint(g, 1000, 750);

        sendImage(bufferedImage, parameters.getE());
    }
}
