package core.commands.charts;

import core.apis.last.entities.chartentities.*;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChartDecadeParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartYearRangeParameters;
import core.util.ServiceView;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONObject;
import org.knowm.xchart.PieChart;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class AOTDCommand extends ChartableCommand<ChartYearRangeParameters> {
    private final MusicBrainzService mb;
    private final int searchSpace = 1500;

    public AOTDCommand(ServiceView dao) {
        super(dao);
        mb = MusicBrainzServiceSingleton.getInstance();

    }

    @Override
    public ChartableParser<ChartYearRangeParameters> initParser() {
        return new ChartDecadeParser(db);
    }

    @Override
    public String getSlashName() {
        return "aotd";
    }

    @Override
    public String getDescription() {
        return "Like AOTY but for multiple years at the same time";
    }

    @Override
    public List<String> getAliases() {
        return List.of("aotd", "range");
    }

    @Override
    public String getName() {
        return "Album Of The Decade";
    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartYearRangeParameters param) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
        boolean isByTime = param.isByTime();
        List<AlbumInfo> nonEmptyMbid;
        List<AlbumInfo> emptyMbid;

        if (!isByTime && param.getTimeFrameEnum().isAllTime()) {
            List<ScrobbledAlbum> userAlbumByMbid = db.getUserAlbums(param.getUser().getName());
            AtomicInteger atomicInteger = new AtomicInteger(0);

            nonEmptyMbid = userAlbumByMbid.stream().peek(x -> queue.add(new AlbumChart(x.getUrl(), atomicInteger.getAndIncrement(), x.getAlbum(), x.getArtist(), x.getAlbumMbid(), x.getCount(), param.isWriteTitles(), param.isWritePlays(), param.isAside())))
                    .map(x -> new AlbumInfo(x.getAlbumMbid(), x.getAlbum(), x.getArtist()))
                    .filter(albumInfo -> !(albumInfo.getMbid() == null || albumInfo.getMbid().isEmpty()))
                    .toList();
            emptyMbid = Collections.emptyList();
        } else {
            BiFunction<JSONObject, Integer, UrlCapsule> parser;
            TimeFrameEnum timeFrameEnum = param.getTimeFrameEnum().getTimeFrameEnum();
            if (timeFrameEnum.equals(TimeFrameEnum.DAY)) {
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
            Map<Boolean, List<AlbumInfo>> results =
                    queue.stream()
                            .map(capsule ->
                                    new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName()))
                            .collect(Collectors.partitioningBy(albumInfo -> albumInfo.getMbid().isEmpty()));

            nonEmptyMbid = results.get(false);
            emptyMbid = results.get(true);
        }

        int baseYear = param.getBaseYear().getValue();
        List<AlbumInfo> albumsMbizMatchingYear;
        if (isByTime) {
            return handleTimedChart(param, nonEmptyMbid, emptyMbid, queue);
        }
        albumsMbizMatchingYear = mb.listOfYearRangeReleases(nonEmptyMbid, baseYear, param.getNumberOfYears());
        List<AlbumInfo> mbFoundBYName = mb.findArtistByReleaseRangeYear(emptyMbid, baseYear, param.getNumberOfYears());
        emptyMbid.removeAll(mbFoundBYName);


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
        db.updateMetrics(0, mbFoundBYName.size(), albumsMbizMatchingYear
                .size(), ((long) param.getX()) * param.getX());
        return new CountWrapper<>(albumsMbizMatchingYear.size(), queue);
    }


    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartYearRangeParameters params, int count) {
        return params.initEmbed("s top albums from " + params.getDisplayString(), embedBuilder, " has " + count + " albums from " + params.getDisplayString() + " in their top " + searchSpace + " albums", params.getUser().getName());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartYearRangeParameters params, int count, String initTitle) {
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(String.format("%ss top albums from the %s%s", initTitle, params.getDisplayString(), time));
        return String.format("%s has %d albums from %s in their top %d albums%s (showing top %d)", initTitle, count, params.getDisplayString(), searchSpace, time, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(ChartYearRangeParameters parameters) {
        Context e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoEscaped(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("Couldn't find any %s album in %s top %d albums%s!", parameters.getDisplayString(), ingo.username(), searchSpace, parameters.getTimeFrameEnum().getDisplayString()));
    }

    @Override
    public void doImage(BlockingQueue<UrlCapsule> queue, int x, int y, ChartYearRangeParameters parameters, int size) {
        if (!parameters.isCareAboutSized()) {
            int imageSize = Math.max((int) Math.ceil(Math.sqrt(queue.size())), 1);
            if (queue.size() > 1000) {
                BlockingQueue<UrlCapsule> tempQueuenew = new LinkedBlockingDeque<>();
                queue.drainTo(tempQueuenew, 40 * 40);
                queue = tempQueuenew;
            }
            super.doImage(queue, imageSize, imageSize, parameters, size);
        } else {
            BlockingQueue<UrlCapsule> tempQueuenew = new LinkedBlockingDeque<>();
            queue.drainTo(tempQueuenew, x * y);
            super.doImage(tempQueuenew, x, y, parameters, size);
        }
    }


    private CountWrapper<BlockingQueue<UrlCapsule>> handleTimedChart(ChartYearRangeParameters parameters, List<AlbumInfo> nonEmptyMbid, List<AlbumInfo> emptyMbid, BlockingQueue<UrlCapsule> queue) {
        List<AlbumInfo> albumsMbizMatchingYear;
        int baseYear = parameters.getBaseYear().getValue();

        List<CountWrapper<AlbumInfo>> accum = mb.listOfRangeYearReleasesWithAverage(nonEmptyMbid, baseYear, parameters.getNumberOfYears());
        List<CountWrapper<AlbumInfo>> mbFoundBYName = mb.findArtistByReleaseWithAverageRangeYears(emptyMbid, baseYear, parameters.getNumberOfYears());
        emptyMbid.removeAll(mbFoundBYName.stream().map(CountWrapper::getResult).toList());


        albumsMbizMatchingYear = accum.stream().map(CountWrapper::getResult).toList();
        accum.addAll(mbFoundBYName);
        albumsMbizMatchingYear.addAll(mbFoundBYName.stream().map(CountWrapper::getResult).toList());

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

        BlockingQueue<UrlCapsule> sortedQ = b.stream().sorted(Comparator.comparing(x -> (
                        ((TrackDurationAlbumArtistChart) x).getSeconds()
                )).reversed()).peek(x -> x.setPos(asdasdasd.getAndIncrement()))
                .collect(Collectors.toCollection(LinkedBlockingDeque::new));
        return new CountWrapper<>(albumsMbizMatchingYear.size(), sortedQ);
    }
}
