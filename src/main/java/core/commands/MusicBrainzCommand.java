package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.apis.last.chartentities.TrackDurationAlbumArtistChart;
import core.exceptions.LastFmException;
import core.parsers.ChartYearParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.UrlCapsule;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.time.Year;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MusicBrainzCommand extends ChartableCommand<ChartYearParameters> {
    private final DiscogsApi discogsApi;
    private final MusicBrainzService mb;
    public int searchSpace = 100;


    public MusicBrainzCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        mb = MusicBrainzServiceSingleton.getInstance();
    }

    @Override
    public ChartableParser<ChartYearParameters> getParser() {
        return new ChartYearParser(getService(), searchSpace);
    }


    @Override
    public String getName() {
        return "Released in YEAR";
    }

    @Override
    public String getDescription() {
        return "Gets your top albums from the time frame provided and check if they were released in the provided year";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("releaseyear");
    }


    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartYearParameters params) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
        boolean isByTime = params.isByTime();


        lastFM.getChart(params.getLastfmID(), params.getTimeFrameEnum().toApiFormat(), this.searchSpace, 1, TopEntity.ALBUM, isByTime ? TrackDurationAlbumArtistChart.getAlbumParser(params) : AlbumChart.getAlbumParser(params), queue);
        Year year = params.getYear();
        //List of obtained elements
        Map<Boolean, List<AlbumInfo>> results =
                queue.stream()
                        .map(capsule ->
                                new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName()))
                        .collect(Collectors.partitioningBy(albumInfo -> albumInfo.getMbid().isEmpty()));

        List<AlbumInfo> nonEmptyMbid = results.get(false);
        List<AlbumInfo> emptyMbid = results.get(true);


        List<AlbumInfo> albumsMbizMatchingYear;
        if (isByTime) {
            return handleTimedChart(params, nonEmptyMbid, emptyMbid, queue);
        }
        albumsMbizMatchingYear = mb.listOfYearReleases(nonEmptyMbid, year);
        List<AlbumInfo> mbFoundBYName = mb.findArtistByRelease(emptyMbid, year);
        emptyMbid.removeAll(mbFoundBYName);

        int discogsMetrics = 0;
        if (doDiscogs()) {
            List<AlbumInfo> foundDiscogsMatchingYear = emptyMbid.stream().filter(albumInfo -> {
                try {

                    Year tempYear = (discogsApi.getYearRelease(albumInfo.getName(), albumInfo.getArtist()));
                    if (tempYear == null) {
                        return false;
                    }
                    return tempYear.equals(year);
                } catch (Exception ex) {
                    return false;
                }
            }).collect(Collectors.toList());
            albumsMbizMatchingYear.addAll(foundDiscogsMatchingYear);
            discogsMetrics = foundDiscogsMatchingYear.size();
        }

        //Keep the order of the original queue so the final chart is ordered by plays
        AtomicInteger counter2 = new AtomicInteger(0);
        queue.removeIf(urlCapsule -> {
            for (AlbumInfo albumInfo : albumsMbizMatchingYear) {
                if ((!albumInfo.getMbid().isEmpty() && albumInfo.getMbid().equals(urlCapsule.getMbid())) || urlCapsule
                                                                                                                    .getAlbumName().equalsIgnoreCase(albumInfo.getName()) && urlCapsule.getArtistName()
                                                                                                                    .equalsIgnoreCase(albumInfo.getArtist())) {
                    urlCapsule.setPos(counter2.getAndAdd(1));
                    return false;
                }
            }
            return true;
        });
        getService().updateMetrics(discogsMetrics, mbFoundBYName.size(), albumsMbizMatchingYear
                .size(), ((long) params.getX()) * params.getX());
        return new CountWrapper<>(albumsMbizMatchingYear.size(), queue);
    }


    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartYearParameters params, int count) {
        Year year = params.getYear();
        return params.initEmbed("s top albums from " + year.toString(), embedBuilder, " has " + count + " albums from " + year.toString() + " in their top " + searchSpace + " albums");
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartYearParameters params, int count, String initTitle) {
        Year year = params.getYear();
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(String.format("%ss top albums from %s%s", initTitle, year.toString(), time));
        return String.format("%s has %d albums from %s in their top %d albums%s (showing top %d)", initTitle, count, year.toString(), searchSpace, time, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartYearParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("Couldn't find any %s album in %s top %d albums%s!", parameters.getYear().toString(), ingo.getUsername(), searchSpace, parameters.getTimeFrameEnum().getDisplayString()));
    }

    @Override
    public void doImage(BlockingQueue<UrlCapsule> queue, int x, int y, ChartYearParameters parameters) {
        if (!parameters.isCareAboutSized()) {
            int imageSize = Math.max((int) Math.ceil(Math.sqrt(queue.size())), 1);
            super.doImage(queue, imageSize, imageSize, parameters);
        } else {
            BlockingQueue<UrlCapsule> tempQueuenew = new LinkedBlockingDeque<>();
            queue.drainTo(tempQueuenew, x * y);
            super.doImage(tempQueuenew, x, y, parameters);
        }
    }

    boolean doDiscogs() {
        return true;

    }

    private CountWrapper<BlockingQueue<UrlCapsule>> handleTimedChart(ChartYearParameters parameters, List<AlbumInfo> nonEmptyMbid, List<AlbumInfo> emptyMbid, BlockingQueue<UrlCapsule> queue) {
        List<AlbumInfo> albumsMbizMatchingYear;
        Year year = parameters.getYear();

        List<CountWrapper<AlbumInfo>> accum = mb.listOfYearReleasesWithAverage(nonEmptyMbid, year);
        List<CountWrapper<AlbumInfo>> mbFoundBYName = mb.findArtistByReleaseWithAverage(emptyMbid, year);
        emptyMbid.removeAll(mbFoundBYName.stream().map(CountWrapper::getResult).collect(Collectors.toList()));


        albumsMbizMatchingYear = accum.stream().map(CountWrapper::getResult).collect(Collectors.toList());
        accum.addAll(mbFoundBYName);
        albumsMbizMatchingYear.addAll(mbFoundBYName.stream().map(CountWrapper::getResult).collect(Collectors.toList()));

        List<UrlCapsule> b = new ArrayList<>();
        queue.drainTo(b);

        b.removeIf(urlCapsule -> {
            for (CountWrapper<AlbumInfo> t : accum) {
                AlbumInfo albumInfo = t.getResult();

                if ((!albumInfo.getMbid().isEmpty() && albumInfo.getMbid().equals(urlCapsule.getMbid())) || urlCapsule.getAlbumName().equalsIgnoreCase(albumInfo.getName()) && urlCapsule.getArtistName()
                        .equalsIgnoreCase(albumInfo.getArtist())) {
                    TrackDurationAlbumArtistChart urlCapsule1 = (TrackDurationAlbumArtistChart) urlCapsule;
                    urlCapsule1.setSeconds((t.getRows() / 1000) * urlCapsule.getPlays());
                    return false;
                }
            }
            return true;
        });
        AtomicInteger asdasdasd = new AtomicInteger(0);

        LinkedBlockingDeque<UrlCapsule> collect = b.stream().sorted(Comparator.comparing(x -> (
                ((TrackDurationAlbumArtistChart) x).getSeconds()
        )).reversed()).peek(x -> x.setPos(asdasdasd.getAndIncrement()))
                .collect(Collectors.toCollection(LinkedBlockingDeque::new));
        return new CountWrapper<>(albumsMbizMatchingYear.size(), collect);
    }


}
