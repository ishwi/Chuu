package core.commands.stats;

import core.apis.last.entities.chartentities.AlbumChart;
import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.charts.ChartableCommand;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.GenreChartParser;
import core.parsers.params.ChartableGenreParameters;
import core.parsers.utils.CustomTimeFrame;
import core.services.TagAlbumService;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GenreAlbumsCommands extends ChartableCommand<ChartableGenreParameters> {
    private final MusicBrainzService mb;

    public GenreAlbumsCommands(ChuuService dao) {
        super(dao);
        mb = MusicBrainzServiceSingleton.getInstance();
    }

    @Override
    public ChartableParser<ChartableGenreParameters> initParser() {
        return new GenreChartParser(db, TimeFrameEnum.WEEK, lastFM);
    }

    @Override
    public String getDescription() {
        return "Searches Musicbrainz for albums that match the given tag (Should be coherent with the genre command)";
    }

    @Override
    public List<String> getAliases() {
        return List.of("albumgenres", "albg", "alg");
    }

    @Override
    public String getName() {
        return "Albums by Genre";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartableGenreParameters params) throws LastFmException {

        LastFMData user = params.getUser();
        List<AlbumInfo> albums;
        BlockingQueue<UrlCapsule> queue;
        CustomTimeFrame custom = params.getTimeFrameEnum();
        TimeFrameEnum timeFrameEnum = custom.getTimeFrameEnum();
        String genre = params.getGenreParameters().getGenre();
        BlockingQueue<UrlCapsule> outerQueue;
        AtomicInteger ranker = new AtomicInteger(0);
        if (timeFrameEnum.equals(TimeFrameEnum.ALL)) {
            outerQueue = db.getUserAlbumWithTag(user.getDiscordId(), genre,
                    params.getX() * params.getY()
            ).stream().map(t -> new AlbumChart(t.getUrl(), ranker.getAndIncrement(), t.getAlbum(), t.getArtist(), t.getArtistMbid(), t.getCount(),
                    params.isWriteTitles(), params.isWritePlays(), params.isAside()
            )).collect(Collectors.toCollection(LinkedBlockingQueue::new));

        } else {
            queue = new ArrayBlockingQueue<>(4000);

            lastFM.getChart(user, custom, 4000, 1,
                    TopEntity.ALBUM,
                    ChartUtil.getParser(custom, TopEntity.ALBUM, params, lastFM, user), queue);

            Set<AlbumInfo> albumInfos = new HashSet<>(db.getAlbumsWithTags(queue.stream().map(x -> new AlbumInfo(x.getMbid(), x.getAlbumName(), x.getArtistName())).toList(), params.getDiscordId(), genre));


            ArrayList<UrlCapsule> c = new ArrayList<>(queue);
            albums = c.stream()
                    .filter(x -> x.getMbid() != null
                            && !x.getMbid().isBlank()
                    )
                    .map(x -> new AlbumInfo(x.getMbid()))
                    .filter(o -> !albumInfos.contains(o))
                    .toList();
            Set<String> strings = this.mb.albumsGenre(albums, genre);

            outerQueue = queue.stream()
                    .filter(x -> x.getMbid() != null && !x.getMbid().isBlank() && strings.contains(x.getMbid()) || albumInfos.contains(new AlbumInfo(x.getMbid(), x.getAlbumName(), x.getArtistName())))
                    .sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed())
                    .peek(x -> x.setPos(ranker.getAndIncrement()))
                    .limit((long) params.getX() * params.getY())
                    .collect(Collectors.toCollection(LinkedBlockingQueue::new));

            executor.submit(
                    new TagAlbumService(db, lastFM, outerQueue.stream().map(x -> new AlbumInfo(x.getMbid(), x.getAlbumName(), x.getArtistName())).toList(), genre));
        }
        return new CountWrapper<>(ranker.get(), outerQueue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartableGenreParameters params, int count) {
        String footerText = "";
        if (params.getGenreParameters().isAutoDetected()) {
            NowPlayingArtist np = params.getGenreParameters().getNp();
            footerText += "\nThis genre was obtained from " + String.format("%s - %s | %s", np.getArtistName(), np.getSongName(), np.getAlbumName());
        }

        params.initEmbed("'s top " + params.getGenreParameters().getGenre() + " albums", embedBuilder, ""
                , params.getUser().getName());
        DiscordUserDisplay discordUserDisplay = CommandUtil.getUserInfoNotStripped(params.getE(), params.getDiscordId());
        String us = CommandUtil.markdownLessString(discordUserDisplay.getUsername());
        String s = "Showing %s top %d %s albums".formatted(us, count, params.getGenreParameters().getGenre());
        embedBuilder.setFooter(s + params.getTimeFrameEnum().getDisplayString() + footerText);
        return embedBuilder;
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartableGenreParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s top " + params.getGenreParameters().getGenre() + " albums " + time);
        return String.format("%s has listened to %d %s albums%s (showing top %d)", initTitle, count, params.getGenreParameters().getGenre(), time, params.getX() * params.getY());
    }


    @Override
    public void noElementsMessage(ChartableGenreParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("Couldn't find any %s album in %s's albums%s!", parameters.getGenreParameters().getGenre(), ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }
}
