package core.commands;

import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.UrlCapsule;
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
import java.util.concurrent.LinkedBlockingDeque;
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
        return new GenreChartParser(getService(), TimeFrameEnum.WEEK, lastFM);
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

        String name = params.getLastfmID();
        List<AlbumInfo> albums;
        BlockingQueue<UrlCapsule> queue;
        CustomTimeFrame custom = params.getTimeFrameEnum();
        TimeFrameEnum timeFrameEnum = custom.getTimeFrameEnum();
        if (timeFrameEnum.equals(TimeFrameEnum.ALL)) {

            List<ScrobbledAlbum> userAlbumByMbid = getService().getUserAlbumByMbid(name);
            albums = userAlbumByMbid.stream().filter(u -> u.getAlbumMbid() != null && !u.getAlbumMbid().isEmpty()).map(x ->
                    new AlbumInfo(x.getAlbumMbid(), x.getAlbum(), x.getArtist())).collect(Collectors.toList());
            queue = userAlbumByMbid.stream().map(x -> new AlbumChart(x.getUrl(), 0, x.getAlbum(), x.getArtist(), x.getAlbumMbid(), x.getCount(), params.isWriteTitles(), params.isWritePlays(), params.isAside())).collect(Collectors.toCollection(LinkedBlockingDeque::new));


        } else {
            queue = new ArrayBlockingQueue<>(4000);

            lastFM.getChart(name, custom, 4000, 1,
                    TopEntity.ALBUM,
                    ChartUtil.getParser(custom, TopEntity.ALBUM, params, lastFM, name), queue);
            ArrayList<UrlCapsule> c = new ArrayList<>(queue);
            albums = c.stream()
                    .filter(x -> x.getMbid() != null && !x.getMbid().isBlank()).map(x -> new AlbumInfo(x.getMbid())).collect(Collectors.toList());
        }
        List<AlbumInfo> albumsWithTags = getService().getAlbumsWithTags(queue.stream().map(x -> new AlbumInfo(x.getMbid(), x.getAlbumName(), x.getArtistName())).collect(Collectors.toList()), params.getDiscordId(), params.getGenreParameters().getGenre());
        Set<AlbumInfo> albumInfos = new HashSet<>(albumsWithTags);
        albums.removeIf(albumInfos::contains);
        Set<String> strings = this.mb.albumsGenre(albums, params.getGenreParameters().getGenre());

        AtomicInteger ranker = new AtomicInteger(0);
        LinkedBlockingQueue<UrlCapsule> collect1 = queue.stream()
                .filter(x -> x.getMbid() != null && !x.getMbid().isBlank() && strings.contains(x.getMbid()) || albumInfos.contains(new AlbumInfo(x.getMbid(), x.getAlbumName(), x.getArtistName())))
                .sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed())
                .peek(x -> x.setPos(ranker.getAndIncrement()))
                .limit(params.getX() * params.getY())
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));

        executor.submit(
                new TagAlbumService(getService(), lastFM, collect1.stream().map(x -> new AlbumInfo(x.getMbid(), x.getAlbumName(), x.getArtistName())).collect(Collectors.toList()), params.getGenreParameters().getGenre()));
        return new CountWrapper<>(strings.size(), collect1);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartableGenreParameters params, int count) {
        String footerText = "";
        if (params.getGenreParameters().isAutoDetected()) {
            NowPlayingArtist np = params.getGenreParameters().getNp();
            footerText += "\nThis genre was obtained from " + String.format("%s - %s | %s", np.getArtistName(), np.getSongName(), np.getAlbumName());
        }

        params.initEmbed("'s top " + params.getGenreParameters().getGenre() + " albums", embedBuilder, ""
                , params.getLastfmID());
        String s = " has listened to " + count + " " + params.getGenreParameters().getGenre() + " albums";
        DiscordUserDisplay discordUserDisplay = CommandUtil.getUserInfoNotStripped(params.getE(), params.getDiscordId());
        embedBuilder.setFooter(CommandUtil.markdownLessString(discordUserDisplay.getUsername()) + s + params.getTimeFrameEnum().getDisplayString() + footerText);
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
        sendMessageQueue(e, String.format("Couldn't find any %s album in %s's top %d albums%s!", parameters.getGenreParameters().getGenre(), ingo.getUsername(), 4000, parameters.getTimeFrameEnum().getDisplayString()));
    }
}
