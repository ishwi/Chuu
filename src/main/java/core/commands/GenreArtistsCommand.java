package core.commands;

import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.ArtistChart;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.UrlCapsule;
import core.apis.last.queues.ArtistQueue;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.ChartableParser;
import core.parsers.GenreChartParser;
import core.parsers.params.ChartableGenreParameters;
import core.services.TagArtistService;
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

public class GenreArtistsCommand extends ChartableCommand<ChartableGenreParameters> {
    private final MusicBrainzService mb;

    public GenreArtistsCommand(ChuuService dao) {
        super(dao);
        mb = MusicBrainzServiceSingleton.getInstance();
    }

    @Override
    public ChartableParser<ChartableGenreParameters> initParser() {
        return new GenreChartParser(getService(), TimeFrameEnum.WEEK, lastFM);
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

        String name = params.getLastfmID();
        List<ArtistInfo> artists;
        BlockingQueue<UrlCapsule> queue;
        if (params.getTimeFrameEnum().equals(TimeFrameEnum.ALL)) {

            List<ScrobbledArtist> userAlbumByMbid = getService().getUserArtistByMbid(name);
            artists = userAlbumByMbid.stream().filter(u -> u.getArtistMbid() != null && !u.getArtistMbid().isEmpty()).map(x ->
                    new ArtistInfo(x.getUrl(), x.getArtist(), x.getArtistMbid())).collect(Collectors.toList());
            queue = userAlbumByMbid.stream().map(x -> new ArtistChart(x.getUrl(), 0, x.getArtist(), x.getArtistMbid(), x.getCount(), params.isWriteTitles(), params.isWritePlays())).collect(Collectors.toCollection(LinkedBlockingDeque::new));


        } else {
            queue = new ArrayBlockingQueue<>(4000);

            lastFM.getChart(name, params.getTimeFrameEnum().toApiFormat(), 4000, 1,
                    TopEntity.ARTIST,
                    ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ARTIST, params, lastFM, name), queue);
            ArrayList<UrlCapsule> c = new ArrayList<>(queue);
            artists = c.stream()
                    .filter(x -> x.getMbid() != null && !x.getMbid().isBlank()).map(x -> {
                        ArtistInfo artistInfo = new ArtistInfo(null, x.getArtistName());
                        artistInfo.setMbid(x.getMbid());
                        return artistInfo;
                    }).collect(Collectors.toList());
        }
        List<ArtistInfo> albumsWithTags = getService().getArtistWithTag(queue.stream().map(x -> new ArtistInfo(x.getUrl(), x.getArtistName())).collect(Collectors.toList()), params.getDiscordId(), params.getGenreParameters().getGenre());
        Map<ArtistInfo, ArtistInfo> identityMap = albumsWithTags.stream().collect(Collectors.toMap(x -> x, x -> x, (x, y) -> x));
        artists.removeIf(identityMap::containsKey);
        Set<String> strings = this.mb.artistGenres(artists, params.getGenreParameters().getGenre());

        AtomicInteger ranker = new AtomicInteger(0);
        LinkedBlockingQueue<UrlCapsule> collect1 = queue.stream()
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
                .limit(params.getX() * params.getY())
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        ArtistQueue artistQueue = new ArtistQueue(getService(), DiscogsSingleton.getInstanceUsingDoubleLocking(), SpotifySingleton.getInstance());
        artistQueue.addAll(collect1);
        executor.submit(
                new TagArtistService(getService(), lastFM, collect1.stream().map(x -> new ArtistInfo(null, x.getArtistName(), x.getMbid())).collect(Collectors.toList()), params.getGenreParameters().getGenre()));
        return new CountWrapper<>(strings.size(), artistQueue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartableGenreParameters params, int count) {
        String footerText = "";
        if (params.getGenreParameters().isAutoDetected()) {
            NowPlayingArtist np = params.getGenreParameters().getNp();
            footerText += "\nThis genre was obtained from " + String.format("%s - %s | %s", np.getArtistName(), np.getSongName(), np.getAlbumName());
        }

        params.initEmbed("'s top " + params.getGenreParameters().getGenre() + " artists", embedBuilder, ""
                , params.getLastfmID());
        String s = " has listened to " + count + " " + params.getGenreParameters().getGenre() + " artists";
        DiscordUserDisplay discordUserDisplay = CommandUtil.getUserInfoNotStripped(params.getE(), params.getDiscordId());
        embedBuilder.setFooter(CommandUtil.markdownLessString(discordUserDisplay.getUsername()) + s + params.getTimeFrameEnum().getDisplayString() + footerText);
        return embedBuilder;
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartableGenreParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s top " + params.getGenreParameters().getGenre() + " artists " + time);
        return String.format("%s has listened to %d %s artists%s (showing top %d)", initTitle, count, params.getGenreParameters().getGenre(), time, params.getX() * params.getY());
    }


    @Override
    public void noElementsMessage(ChartableGenreParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay display = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("Couldn't find any %s album in %s's top %d artists%s!", parameters.getGenreParameters().getGenre(), display.getUsername(), 4000, parameters.getTimeFrameEnum().getDisplayString()));
    }
}

