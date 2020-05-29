package core.commands;

import com.neovisionaries.i18n.CountryCode;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.ChartableGenreParameters;
import core.parsers.GenreChartParser;
import core.parsers.params.CountryParameters;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
    public ChartableParser<ChartableGenreParameters> getParser() {
        return new GenreChartParser(getService(), TimeFrameEnum.WEEK, lastFM);
    }

    @Override
    public String getDescription() {
        return "Searches Musicbrainz for artists that match the given tag (Should be coherent with the genre command)";
    }

    @Override
    public List<String> getAliases() {
        return List.of("albumgenres", "ag");
    }

    @Override
    public String getName() {
        return "Artist Of Genre";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartableGenreParameters params) throws LastFmException {

        BlockingQueue<UrlCapsule> queue = new ArrayBlockingQueue<>(4000);
        String name = params.getLastfmID();
        lastFM.getChart(name, params.getTimeFrameEnum().toApiFormat(), 4000, 1,
                TopEntity.ALBUM,
                ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ALBUM, ChartParameters.toListParams(), lastFM, name), queue);

        ArrayList<UrlCapsule> c = new ArrayList<>();
        queue.drainTo(c);
        List<AlbumInfo> collect = c.stream()
                .filter(x -> x.getMbid() != null && !x.getMbid().isBlank()).map(x -> new AlbumInfo(x.getMbid())).collect(Collectors.toList());
        Set<String> strings = this.mb.albumsGenre(collect, params.getGenreParameters().getGenre());
        AtomicInteger ranker = new AtomicInteger(0);
        LinkedBlockingQueue<UrlCapsule> collect1 = c.stream()
                .filter(x -> x.getMbid() != null && !x.getMbid().isBlank() && strings.contains(x.getMbid()))
                .sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed())
                .peek(x -> x.setPos(ranker.getAndIncrement()))
                .limit(params.getX() * params.getY())
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
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
        pieChart.setTitle("'s top " + params.getGenreParameters().getGenre() + " albums " + time);
        return String.format("%s has listened to %d %s albums%s (showing top %d)", initTitle, count, params.getGenreParameters().getGenre(), time, params.getX() * params.getY());
    }


    @Override
    public void noElementsMessage(ChartableGenreParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("Couldn't find any %s album in %s's top %d albums%s!", parameters.getGenreParameters().getGenre(), ingo.getUsername(), 4000, parameters.getTimeFrameEnum().getDisplayString()));
    }
}
