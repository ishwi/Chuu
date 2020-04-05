package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.exceptions.LastFmException;
import core.parsers.ChartFromYearParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MusicBrainzCommand extends ChartableCommand {
    private final DiscogsApi discogsApi;
    private final MusicBrainzService mb;
    public int searchSpace = 100;


    public MusicBrainzCommand(ChuuService dao) {
        super(dao);
        this.parser = new ChartFromYearParser(dao);//
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        mb = MusicBrainzServiceSingleton.getInstance();
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
    public ChartParameters getParameters(String[] returned, MessageReceivedEvent e) {
        Year year = Year.of(Integer.parseInt(returned[0]));
        long discordId = Long.parseLong(returned[1]);
        String username = returned[2];
        String time = returned[3];
        int x = (int) Math.sqrt(searchSpace);
        return new ChartYearParameters(returned, username, discordId, TimeFrameEnum.fromCompletePeriod(time), x, x, e, year);
    }


    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters params) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
        lastFM.getChart(params.getUsername(), params.getTimeFrameEnum().toApiFormat(), searchSpace, 1, TopEntity.ALBUM, AlbumChart.getAlbumParser(params), queue);
        ChartYearParameters chartYearParameters = (ChartYearParameters) params;
        Year year = chartYearParameters.getYear();
        //List of obtained elements
        Map<Boolean, List<AlbumInfo>> results =
                queue.stream()
                        .map(capsule ->
                                new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName()))
                        .collect(Collectors.partitioningBy(albumInfo -> albumInfo.getMbid().isEmpty()));

        List<AlbumInfo> nonEmptyMbid = results.get(false);
        List<AlbumInfo> emptyMbid = results.get(true);

        //List<AlbumInfo> nullYearList = new ArrayList<>();
        List<AlbumInfo> albumsMbizMatchingYear = mb.listOfYearReleases(nonEmptyMbid, year);
        List<AlbumInfo> mbFoundBYName = mb.findArtistByRelease(emptyMbid, year);
        emptyMbid.removeAll(mbFoundBYName);
        List<AlbumInfo> artistByReleaseLower = mb.findArtistByRelease(emptyMbid, year);
        emptyMbid.removeAll(artistByReleaseLower);

        albumsMbizMatchingYear.addAll(mbFoundBYName);
        albumsMbizMatchingYear.addAll(artistByReleaseLower);

        int discogsMetrics = 0;
        if (doDiscogs()) {
            List<AlbumInfo> foundDiscogsMatchingYear = emptyMbid.stream().filter(albumInfo -> {
                try {

                    Year tempYear = (discogsApi.getYearRelease(albumInfo.getName(), albumInfo.getArtist()));
                    if (tempYear == null) {
                        //nullYearList.add(albumInfo);
                        return false;
                    }
                    return tempYear.equals(year);
                } catch (Exception ex) {
                    //Chuu.getLogger().warn(e.getMessage(), e);
                    //nullYearList.add(albumInfo);
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
                .size(), ((long) chartYearParameters.getX()) * chartYearParameters.getX());

        return new CountWrapper<>(albumsMbizMatchingYear.size(), queue);
    }


    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        Year year = ((ChartYearParameters) params).getYear();
        return params.initEmbed("s top albums from " + year.toString(), embedBuilder, " has " + count + " albums from " + year.toString() + " in their top " + searchSpace + " albums");
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        Year year = ((ChartYearParameters) params).getYear();
        String time = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(String.format("%ss top albums from %s%s", initTitle, year.toString(), time));
        return String.format("%s has %d albums from %s in their top %d albums%s (showing top %d)", initTitle, count, year.toString(), searchSpace, time, params.getX() * params.getY());
    }

    @Override
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters) {
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        ChartYearParameters parmas = (ChartYearParameters) parameters;
        sendMessageQueue(e, String.format("Couldn't find any %s album in %s top %d albums%s!", parmas.getYear().toString(), ingo.getUsername(), searchSpace, parameters.getTimeFrameEnum().getDisplayString()));
    }

    @Override
    public void doImage(BlockingQueue<UrlCapsule> queue, int x, int y, ChartParameters parameters) {
        ChartYearParameters yearParameters = (ChartYearParameters) parameters;
        if (!yearParameters.isCareAboutSized()) {
            int imageSize = (int) Math.ceil(Math.sqrt(queue.size()));
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


}
