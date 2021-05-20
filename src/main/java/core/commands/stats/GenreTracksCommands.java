package core.commands.stats;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.TrackChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.commands.charts.ChartableCommand;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.GenreChartParser;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartableGenreParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ServiceView;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GenreTracksCommands extends ChartableCommand<ChartableGenreParameters> {

    public GenreTracksCommands(ServiceView dao) {
        super(dao, true);
    }

    @Override
    public ChartableParser<ChartableGenreParameters> initParser() {

        GenreChartParser genreChartParser = new GenreChartParser(db, TimeFrameEnum.WEEK, lastFM);
        genreChartParser.replaceOptional("list", new OptionalEntity("image", "show this with a chart instead of a list "));
        genreChartParser.addOptional(new OptionalEntity("list", "shows this in list mode", true, Set.of("image", "pie")));
        return genreChartParser;
    }

    @Override
    public String getDescription() {
        return "Searches Musicbrainz for songs that match the given tag";
    }

    @Override
    public List<String> getAliases() {
        return List.of("trackgenres", "songgenres", "sg", "trg");
    }

    @Override
    public String getName() {
        return "Songs by genre";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartableGenreParameters params) throws LastFmException {

        LastFMData user = params.getUser();
        List<TrackInfo> tracks;
        BlockingQueue<UrlCapsule> queue;
        CustomTimeFrame custom = params.getTimeFrameEnum();
        TimeFrameEnum timeFrameEnum = custom.getTimeFrameEnum();
        String genre = params.getGenreParameters().getGenre();
        BlockingQueue<UrlCapsule> outerQueue;
        AtomicInteger ranker = new AtomicInteger(0);
        if (timeFrameEnum.equals(TimeFrameEnum.ALL)) {
            outerQueue = db.getUserTracksWithTag(user.getDiscordId(), genre, params.getX() * params.getY())
                    .stream().map(t -> new TrackChart(t.getUrl(), ranker.getAndIncrement(), t.getName(), t.getArtist(), t.getArtistMbid(), t.getCount(),
                            params.isWriteTitles(), params.isWritePlays(), params.isAside()
                    )).collect(Collectors.toCollection(LinkedBlockingQueue::new));

        } else {
            queue = new ArrayBlockingQueue<>(4000);
            lastFM.getChart(user, custom, 4000, 1,
                    TopEntity.TRACK,
                    ChartUtil.getParser(custom, TopEntity.TRACK, params, lastFM, user), queue);

            ArrayList<UrlCapsule> c = new ArrayList<>(queue);
            tracks = c.stream()
                    .map(x -> new TrackInfo(x.getArtistName(), null, x.getAlbumName(), null))
                    .collect(Collectors.toCollection(ArrayList::new));

            Set<TrackInfo> trackInfoes = new HashSet<>(db.getTrackWithTags(tracks, params.getDiscordId(), genre));
            tracks.removeIf(trackInfoes::contains);

            outerQueue = queue.stream()
                    .filter(x -> trackInfoes.contains(new TrackInfo(x.getArtistName(), null, x.getAlbumName(), null)))
                    .sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed())
                    .peek(x -> x.setPos(ranker.getAndIncrement()))
                    .limit((long) params.getX() * params.getY())
                    .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        }
        return new CountWrapper<>(ranker.get(), outerQueue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartableGenreParameters params, int count) {
        String footerText = "";
        if (params.getGenreParameters().isAutoDetected()) {
            NowPlayingArtist np = params.getGenreParameters().getNp();
            footerText += "\nThis genre was obtained from " + String.format("%s - %s | %s", np.artistName(), np.songName(), np.albumName());
        }

        params.initEmbed("'s top " + params.getGenreParameters().getGenre() + " tracks", embedBuilder, ""
                , params.getUser().getName());
        DiscordUserDisplay discordUserDisplay = CommandUtil.getUserInfoNotStripped(params.getE(), params.getDiscordId());
        String us = CommandUtil.markdownLessString(discordUserDisplay.getUsername());
        String s = "Showing %s top %d %s tracks".formatted(us, count, params.getGenreParameters().getGenre());
        embedBuilder.setFooter(s + params.getTimeFrameEnum().getDisplayString() + footerText);
        return embedBuilder;
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartableGenreParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s top " + params.getGenreParameters().getGenre() + " tracks " + time);
        return String.format("%s has listened to %d %s tracks%s (showing top %d)", initTitle, count, params.getGenreParameters().getGenre(), time, params.getX() * params.getY());
    }


    @Override
    public void noElementsMessage(ChartableGenreParameters parameters) {
        Context e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("Couldn't find any %s track in %s's tracks%s!", parameters.getGenreParameters().getGenre(), ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }
}
