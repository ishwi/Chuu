package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.AlbumChart;
import core.apis.last.queues.ArtistQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ChartFromYearParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.CountWrapper;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MusicBrainzCommand extends ChartableCommand {
    private final DiscogsApi discogsApi;
    private final MusicBrainzService mb;
    private final Spotify spotifyApi;
    public int searchSpace = 100;


    public MusicBrainzCommand(ChuuService dao) {
        super(dao);
        this.parser = new ChartFromYearParser(dao);//
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstanceUsingDoubleLocking();
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
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned;
        returned = parser.parse(e);
        if (returned == null)
            return;


        Year year = Year.of(Integer.parseInt(returned[0]));
        long discordId = Long.parseLong(returned[1]);
        String username = returned[2];
        String time = returned[3];
        boolean titleWrite = !Boolean.parseBoolean(returned[5]);
        boolean playsWrite = Boolean.parseBoolean(returned[6]);
        boolean isList = Boolean.parseBoolean(returned[7]);
        int x = (int) Math.sqrt(searchSpace);
        ChartYearParameters chartParameters = new ChartYearParameters(username, discordId, TimeFrameEnum.fromCompletePeriod(time), x, x, e, titleWrite, playsWrite, isList, year, false);
        CountWrapper<BlockingQueue<UrlCapsule>> result = processQueue(chartParameters);
        BlockingQueue<UrlCapsule> queue = result.getResult();

        if (isList) {
            ArrayList<UrlCapsule> liste = new ArrayList<>(queue.size());
            queue.drainTo(liste);
            doList(liste, chartParameters, result.getRows());
        } else {
            int imageSize = (int) Math.ceil(Math.sqrt(queue.size()));
            doImage(queue, imageSize, imageSize, chartParameters);
        }


    }

    @Override
    public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartParameters params) throws LastFmException {
        BlockingQueue<UrlCapsule> queue = new ArtistQueue(getService(), discogsApi, spotifyApi, !params.isList());
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
    public void noElementsMessage(MessageReceivedEvent e, ChartParameters parameters) {
        ChartYearParameters chartYearParameters = (ChartYearParameters) parameters;
        sendMessageQueue(e, "Dont have any " + chartYearParameters.getYear().toString() + " album in your top " + searchSpace + " albums");
    }


    boolean doDiscogs() {
        return true;

    }


}
