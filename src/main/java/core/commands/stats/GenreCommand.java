package core.commands.stats;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.util.IPieableMap;
import core.otherlisteners.Reactionary;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.NumberParameters;
import core.parsers.params.TimeFrameParameters;
import core.parsers.utils.CustomTimeFrame;
import core.services.TagAlbumService;
import core.services.TagArtistService;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.WordUtils;
import org.knowm.xchart.PieChart;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GenreCommand extends ConcurrentCommand<NumberParameters<TimeFrameParameters>> {
    private final MusicBrainzService musicBrainz;


    private final IPieableMap<Genre, Integer, NumberParameters<TimeFrameParameters>> pieable;

    public GenreCommand(ChuuService dao) {
        super(dao);
        this.musicBrainz = MusicBrainzServiceSingleton.getInstance();
        pieable = (chart, params, data) -> {
            data.entrySet().stream().sorted(((o1, o2) -> o2.getValue().compareTo(o1.getValue()))).sequential().limit(params.getExtraParam())
                    .forEach(entry -> {
                        Genre genre = entry.getKey();
                        int plays = entry.getValue();
                        chart.addSeries(genre.getGenreName() + "\u200B", plays);
                    });
            return chart;

        };
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<TimeFrameParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be between 1 and a big number");
        String s = "You can also introduce a number to vary the number of genres shown in the pie," +
                "defaults to 10";

        TimerFrameParser timerFrameParser = new TimerFrameParser(db, TimeFrameEnum.ALL);
        timerFrameParser.addOptional(new OptionalEntity("artist", "use artists instead of albums for the genres"));
        timerFrameParser.addOptional(new OptionalEntity("lastfm", "use lastfm tags instead of musicbrainz"));
        timerFrameParser.addOptional(new OptionalEntity("mix", "use both lastfm and musicbrainz tags"));


        timerFrameParser.addOptional(new OptionalEntity("list", "display in list format"));


        return new NumberParser<>(timerFrameParser,
                10L,
                Integer.MAX_VALUE,
                map, s, false, true, false);
    }

    @Override
    public String getDescription() {
        return "Top 10 genres from an user";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("genre");
    }

    @Override
    public String getName() {
        return "Top Genres";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<TimeFrameParameters> params) throws LastFmException {


        TimeFrameParameters innerParams = params.getInnerParams();
        LastFMData user = innerParams.getLastFMData();
        String username = user.getName();
        long discordId = user.getDiscordId();

        TimeFrameEnum timeframe = innerParams.getTime();
        DiscordUserDisplay userInfo = CommandUtil.getUserInfoNotStripped(e, discordId);
        String usableString = userInfo.getUsername();
        String urlImage = userInfo.getUrlImage();
        boolean mix = params.hasOptional("mix");
        boolean lastfm = params.hasOptional("lastfm");
        String service = mix ? "Musicbrainz and Last.fm" : lastfm ? "Last.fm" : "Musicbrainz";
        Map<Genre, Integer> map = new HashMap<>();
        boolean doArtists = params.hasOptional("artist");
        if (doArtists) {

            List<ArtistInfo> artistInfos = lastFM.getTopArtists(user, CustomTimeFrame.ofTimeFrameEnum(timeframe), 3000);
            List<ArtistInfo> mbidArtists = artistInfos.stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
                    .collect(Collectors.toList());
            if (artistInfos.isEmpty()) {
                sendMessageQueue(e, "Was not able to find any genre in " + usableString + "'s top 3000 artists" + innerParams.getTime().getDisplayString() + " on " + service);
                return;
            }
            if (mix || lastfm) {
                map.putAll(db.genreCountsByArtist(artistInfos));
            }
            if (mix || !lastfm) {
                Map<Genre, List<ArtistInfo>> genreListMap = musicBrainz.genreCountByartist(mbidArtists);
                executor.submit(new TagArtistService(db, lastFM, genreListMap));
                map.putAll(genreListMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().size())));
            }

        } else {
            List<AlbumInfo> albumInfos;
            List<AlbumInfo> albumMbids;

            if (timeframe == TimeFrameEnum.ALL) {
                albumInfos = db.getUserAlbumByMbid(username).stream().map(x ->
                        new AlbumInfo(x.getAlbumMbid(), x.getAlbum(), x.getArtist())).collect(Collectors.toList());
                albumMbids = albumInfos.stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty()).collect(Collectors.toList());
            } else {
                albumInfos = lastFM.getTopAlbums(user, CustomTimeFrame.ofTimeFrameEnum(timeframe), 3000);
                albumMbids = albumInfos.stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty()).collect(Collectors.toList());
            }
            if (albumInfos.isEmpty()) {
                sendMessageQueue(e, String.format("Was not able to find any genre in %s's top 3000 albums%s on %s", usableString, innerParams.getTime().getDisplayString(), service));
                return;
            }
            if (mix || lastfm) {
                map.putAll(db.genreCountsByAlbum(albumInfos));
            }
            if (mix || !lastfm) {
                Map<Genre, List<AlbumInfo>> genreListMap = musicBrainz.genreCount(albumMbids);
                executor.submit(new TagAlbumService(db, lastFM, genreListMap));
                map = genreListMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().size()));
            }

        }
        if (map.isEmpty()) {
            sendMessageQueue(e, String.format("Was not able to find any genre in %s's top 3000 %s%s on %s", usableString, doArtists ? "artists" : "albums", innerParams.getTime().getDisplayString(), service));
            return;
        }

        if (params.hasOptional("list")) {
            List<String> collect = map.entrySet()
                    .stream().sorted(((o1, o2) -> o2.getValue().compareTo(o1.getValue()))).map(x -> ". **" + WordUtils.capitalizeFully(CommandUtil.cleanMarkdownCharacter(x.getKey().getGenreName())) + "** - " + x.getValue() + "\n").collect(Collectors.toList());
            if (collect.isEmpty()) {
                sendMessageQueue(e, String.format("Was not able to find any genre in %s's top 3000 %s%s on%s", usableString, doArtists ? "artists" : "albums", innerParams.getTime().getDisplayString(), service));
                return;
            }

            StringBuilder a = new StringBuilder();
            for (int i = 0; i < 10 && i < collect.size(); i++) {
                a.append(i + 1).append(collect.get(i));
            }
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription(a)
                    .setColor(CommandUtil.randomColor())
                    .setTitle(usableString + "'s genres")
                    .setFooter(usableString + " has " + collect.size() + " found genres" + timeframe.getDisplayString(), null)
                    .setThumbnail(urlImage);
            e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                    new Reactionary<>(collect, message1, embedBuilder));


        } else {
            Long extraParam = params.getExtraParam();
            PieChart pieChart = pieable.doPie(params, map);
            pieChart.setTitle("Top " + extraParam + " Genres from " + usableString + timeframe.getDisplayString());
            BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufferedImage.createGraphics();
            GraphicUtils.setQuality(g);
            pieChart.paint(g, 1000, 750);
            GraphicUtils.inserArtistImage(urlImage, g);
            sendImage(bufferedImage, e);
        }
    }


}
