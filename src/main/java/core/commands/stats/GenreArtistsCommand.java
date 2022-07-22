package core.commands.stats;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.entities.chartentities.ArtistChart;
import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.last.queues.ArtistQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.charts.ChartableCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.GenreChartParser;
import core.parsers.params.ChartableGenreParameters;
import core.services.tags.TagArtistService;
import core.util.ServiceView;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GenreArtistsCommand extends ChartableCommand<ChartableGenreParameters> {
    private final MusicBrainzService mb;
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public GenreArtistsCommand(ServiceView dao) {
        super(dao);
        mb = MusicBrainzServiceSingleton.getInstance();
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.GENRES;
    }

    @Override
    public ChartableParser<ChartableGenreParameters> initParser() {
        return new GenreChartParser(db, TimeFrameEnum.WEEK, lastFM);
    }

    @Override
    public String getSlashName() {
        return "artist-chart";
    }

    @Override
    public String getDescription() {
        return "Searches Musicbrainz for artists that match the given tag (Should be coherent with the genre command)";
    }

    @Override
    public List<String> getAliases() {
        return List.of("artistgenres", "ag");
    }

    @Override
    public String getName() {
        return "Artists by Genre";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartableGenreParameters params) throws LastFmException {

        LastFMData name = params.getUser();
        List<ArtistInfo> artists;
        String genre = params.getGenreParameters().getGenre();
        BlockingQueue<UrlCapsule> outerQueue;
        AtomicInteger ranker = new AtomicInteger(0);
        if (params.getTimeFrameEnum().isAllTime()) {
            outerQueue = db.getUserArtistWithTag(name.getDiscordId(), genre, params.getX() * params.getY()).stream().map(t -> new ArtistChart(t.getUrl(), ranker.getAndIncrement(), t.getArtist(), t.getArtistMbid(), t.getCount(),
                            params.isWriteTitles(), params.isWritePlays(),
                            params.isAside()))
                    .limit((long) params.getX() * params.getY())
                    .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        } else {
            BlockingQueue<UrlCapsule> queue;
            queue = new ArrayBlockingQueue<>(4000);

            lastFM.getChart(name, params.getTimeFrameEnum(), 4000, 1,
                    TopEntity.ARTIST,
                    ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ARTIST, params, lastFM, name), queue);


            Map<ArtistInfo, ArtistInfo> identityMap =
                    db.getArtistWithTag(queue.stream().map(x -> new ArtistInfo(x.getUrl(), x.getArtistName())).toList(), params.getDiscordId(), genre)
                            .stream().collect(Collectors.toMap(x -> x, x -> x, (x, y) -> x));

            ArrayList<UrlCapsule> c = new ArrayList<>(queue);
            artists = c.stream()
                    .filter(x -> x.getMbid() != null && !x.getMbid().isBlank())
                    .map(x -> {
                        ArtistInfo artistInfo = new ArtistInfo(null, x.getArtistName());
                        artistInfo.setMbid(x.getMbid());
                        return artistInfo;
                    })
                    .filter(t -> !identityMap.containsKey(t))
                    .toList();

            Set<String> strings = this.mb.artistGenres(artists, genre);

            Queue<UrlCapsule> tempQueue = queue.stream()
                    .filter(x -> {
                        ArtistInfo o = new ArtistInfo(x.getUrl(), x.getArtistName());
                        o.setMbid(x.getMbid());

                        boolean contains = identityMap.containsKey(o);
                        if (contains) {
                            x.setUrl(identityMap.get(o).getArtistUrl());
                        }
                        return x.getMbid() != null && !x.getMbid().isBlank() && strings.contains(x.getMbid()) || contains;
                    })
                    .sorted(Comparator.comparingInt(UrlCapsule::getPlays).reversed())
                    .peek(x -> x.setPos(ranker.getAndIncrement()))
                    .limit((long) params.getX() * params.getY())
                    .collect(Collectors.toCollection(LinkedBlockingQueue::new));
            outerQueue = new ArtistQueue(db, discogsApi, spotify);
            outerQueue.addAll(tempQueue);
            CommandUtil.runLog(new TagArtistService(db, lastFM, tempQueue.stream().map(x -> new ArtistInfo(null, x.getArtistName(), x.getMbid())).collect(Collectors.toList()), genre));

        }
        return new CountWrapper<>(outerQueue.size(), outerQueue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartableGenreParameters params, int count) {
        String footerText = "";
        if (params.getGenreParameters().isAutoDetected()) {
            NowPlayingArtist np = params.getGenreParameters().getNp();
            footerText += "\nThis genre was obtained from " + String.format("%s - %s | %s", np.artistName(), np.songName(), np.albumName());
        }

        params.initEmbed("'s top " + params.getGenreParameters().getGenre() + " artists", embedBuilder, ""
                , params.getUser().getName());
        DiscordUserDisplay discordUserDisplay = CommandUtil.getUserInfoUnescaped(params.getE(), params.getDiscordId());
        String us = CommandUtil.stripEscapedMarkdown(discordUserDisplay.username());
        String s = "Showing %s top %d %s artists".formatted(us, count, params.getGenreParameters().getGenre());
        return embedBuilder
                .setFooter("%s has listened to %d %s artists%s%s".formatted(us, count, params.getGenreParameters().getGenre(), params.getTimeFrameEnum().getDisplayString(), footerText));
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartableGenreParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s top " + params.getGenreParameters().getGenre() + " artists " + time);
        return String.format("%s has listened to %d %s artists%s (showing top %d)", initTitle, count, params.getGenreParameters().getGenre(), time, params.getX() * params.getY());
    }


    @Override
    public void noElementsMessage(ChartableGenreParameters parameters) {
        Context e = parameters.getE();
        DiscordUserDisplay display = CommandUtil.getUserInfoEscaped(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("Couldn't find any %s album in %s's artists%s!", parameters.getGenreParameters().getGenre(), display.username(), parameters.getTimeFrameEnum().getDisplayString()));
    }
}

