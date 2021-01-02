package core.commands.charts;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.TrackDurationAlbumArtistChart;
import core.apis.last.chartentities.UrlCapsule;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartYearParser;
import core.parsers.ChartableParser;
import core.parsers.OptionalEntity;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;
import org.knowm.xchart.PieChart;

import java.time.Year;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
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
    public ChartableParser<ChartYearParameters> initParser() {
        ChartYearParser chartYearParser = new ChartYearParser(getService(), searchSpace);
        chartYearParser.addOptional(new OptionalEntity("time", "make the chart to be sorted by duration (quite inaccurate)"));

        return
                chartYearParser;
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
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartYearParameters param) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();

        boolean isByTime = param.isByTime();
        List<AlbumInfo> nonEmptyMbid;
        List<AlbumInfo> emptyMbid;
        Set<AlbumInfo> albumsMbizMatchingYear = new HashSet<>();

        Year year = param.getYear();
        if (!isByTime && param.getTimeFrameEnum().isAllTime()) {
            List<ScrobbledAlbum> userAlbumByMbid = getService().getUserAlbumsOfYear(param.getUser().getName(), year);
            albumsMbizMatchingYear.addAll(userAlbumByMbid.stream().map(x -> new AlbumInfo(x.getAlbumMbid(), x.getAlbum(), x.getArtist())).collect(Collectors.toList()));

            AtomicInteger atomicInteger = new AtomicInteger(0);
            List<ScrobbledAlbum> userAlbumsWithNoYear = getService().getUserAlbumsWithNoYear(param.getUser().getName());
            userAlbumByMbid.forEach(x -> queue.add(new AlbumChart(x.getUrl(), atomicInteger.getAndIncrement(), x.getAlbum(), x.getArtist(), x.getAlbumMbid(), x.getCount(), param.isWriteTitles(), param.isWritePlays(), param.isAside())));
            userAlbumsWithNoYear.forEach(x -> queue.add(new AlbumChart(x.getUrl(), atomicInteger.getAndIncrement(), x.getAlbum(), x.getArtist(), x.getAlbumMbid(), x.getCount(), param.isWriteTitles(), param.isWritePlays(), param.isAside())));
            Map<Boolean, List<AlbumInfo>> results = userAlbumsWithNoYear.stream()
                    .map(x -> new AlbumInfo(x.getAlbumMbid(), x.getAlbum(), x.getArtist()))
                    .collect(Collectors.partitioningBy(albumInfo -> albumInfo.getMbid() == null || albumInfo.getMbid().isEmpty()));
            nonEmptyMbid = results.get(false);
            emptyMbid = results.get(true);
        } else {
            BiFunction<JSONObject, Integer, UrlCapsule> parser;
            if (param.getTimeFrameEnum().getTimeFrameEnum().equals(TimeFrameEnum.DAY)) {
                if (isByTime)
                    parser = TrackDurationAlbumArtistChart.getDailyArtistAlbumDurationParser(param, lastFM.getTrackDurations(param.getUser(), TimeFrameEnum.WEEK));
                else {
                    parser = AlbumChart.getDailyAlbumParser(param);
                }
            } else {
                if (isByTime)
                    parser = TrackDurationAlbumArtistChart.getTimedParser(param);
                else {
                    parser = ChartUtil.getParser(param.getTimeFrameEnum(), TopEntity.ALBUM, param, lastFM, param.getUser());
                }
            }


            lastFM.getChart(param.getUser(), param.getTimeFrameEnum(), this.searchSpace, 1, TopEntity.ALBUM, parser, queue);
            //List of obtained elements
            List<AlbumInfo> collect = queue.stream()
                    .map(capsule ->
                            new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName())).collect(Collectors.toList());

            List<AlbumInfo> albumInfos = getService().albumsOfYear(collect, param.getYear());
            albumsMbizMatchingYear.addAll(albumInfos);


            Map<Boolean, List<AlbumInfo>> results =
                    queue.stream()

                            .map(capsule ->
                                    new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName()))
                            .filter(x -> !albumsMbizMatchingYear.contains(x))
                            .collect(Collectors.partitioningBy(albumInfo -> albumInfo.getMbid().isEmpty()));
            nonEmptyMbid = results.get(false);
            emptyMbid = results.get(true);

        }
        if (isByTime) {
            return handleTimedChart(param, nonEmptyMbid, emptyMbid, queue);
        }
        List<AlbumInfo> foundByYear = mb.listOfYearReleases(nonEmptyMbid, year);
        CompletableFuture.runAsync(() -> getService().insertAlbumsOfYear(foundByYear, year));

        albumsMbizMatchingYear.addAll(foundByYear);
        List<AlbumInfo> mbFoundBYName = mb.findArtistByRelease(emptyMbid, year);
        CompletableFuture.runAsync(() -> getService().insertAlbumsOfYear(mbFoundBYName, year));
        //CompletableFuture.supplyAsync( x -> getService().insertAlbum(x))
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
            CompletableFuture.runAsync(() -> getService().insertAlbumsOfYear(foundDiscogsMatchingYear, year));
            albumsMbizMatchingYear.addAll(foundDiscogsMatchingYear);
            discogsMetrics = foundDiscogsMatchingYear.size();
        }

        //Keep the order of the original queue so the final chart is ordered by plays
        AtomicInteger counter2 = new AtomicInteger(0);
        queue.removeIf(urlCapsule -> {
            for (AlbumInfo albumInfo : albumsMbizMatchingYear) {
                if ((albumInfo.getMbid() != null && !albumInfo.getMbid().isEmpty() && albumInfo.getMbid().equals(urlCapsule.getMbid())) || urlCapsule
                        .getAlbumName().equalsIgnoreCase(albumInfo.getName()) && urlCapsule.getArtistName()
                        .equalsIgnoreCase(albumInfo.getArtist())) {
                    urlCapsule.setPos(counter2.getAndAdd(1));
                    return false;
                }
            }
            return true;
        });
        getService().updateMetrics(discogsMetrics, mbFoundBYName.size(), albumsMbizMatchingYear
                .size(), ((long) param.getX()) * param.getX());
        return new CountWrapper<>(albumsMbizMatchingYear.size(), queue);
    }


    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartYearParameters params, int count) {
        Year year = params.getYear();
        String s = " in their top " + searchSpace + " albums";
        if (year.getValue() != Year.now().getValue()) {
            s = " in their library";
        }
        return params.initEmbed("s top albums from " + year.toString(), embedBuilder, " has " + count + " albums from " + year.toString() + s, params.getUser().getName());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartYearParameters params, int count, String initTitle) {
        Year year = params.getYear();
        String time = params.getTimeFrameEnum().getDisplayString();
        String s;
        if (year.getValue() != Year.now().getValue()) {
            s = "in their library";
        } else {
            s = "in their top " + searchSpace + " albums";
        }
        pieChart.setTitle(String.format("%ss top albums from %s%s", initTitle, year.toString(), time));
        return String.format("%s has %d albums from %s %s%s (showing top %d)", initTitle, count, year.toString(), s, time, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartYearParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        String s;
        if (parameters.getYear().getValue() != Year.now().getValue()) {
            s = "in their library";
        } else {
            s = "in their top " + searchSpace + " albums";
        }
        sendMessageQueue(e, String.format("Couldn't find any %s album %s%s!", parameters.getYear().toString(), s, parameters.getTimeFrameEnum().getDisplayString()));
    }

    @Override
    public void doImage(BlockingQueue<UrlCapsule> queue, int x, int y, ChartYearParameters parameters, int size) {
        if (!parameters.isCareAboutSized()) {
            int imageSize = Math.max((int) Math.ceil(Math.sqrt(queue.size())), 1);
            super.doImage(queue, imageSize, imageSize, parameters, size);
        } else {
            BlockingQueue<UrlCapsule> tempQueuenew = new LinkedBlockingDeque<>();
            queue.drainTo(tempQueuenew, x * y);
            super.doImage(tempQueuenew, x, y, parameters, size);
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
