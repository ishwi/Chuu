package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.TopEntity;
import core.apis.last.chartentities.ChartUtil;
import core.apis.last.chartentities.UrlCapsule;
import core.apis.last.queues.DiscardableQueue;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.ArtistTimeFrameParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.ArtistTimeFrameParameters;
import core.parsers.params.ChartParameters;
import core.parsers.params.NumberParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.UserInfo;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiFunction;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class PaceArtistCommand extends ConcurrentCommand<NumberParameters<ArtistTimeFrameParameters>> {

    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public PaceArtistCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<ArtistTimeFrameParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can introduce a goal that will be the number of scrobbles that you want to obtain.";
        return new NumberParser<>(new ArtistTimeFrameParser(getService(), lastFM),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true);
    }

    @Override
    public String getDescription() {
        return "Like pace but for a given artists and with more limited time windows";
    }

    @Override
    public List<String> getAliases() {
        return List.of("artistpace", "apace", "pacea");
    }

    @Override
    public String getName() {
        return "Artist Pace";
    }

    @Override
    void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<ArtistTimeFrameParameters> params) throws LastFmException, InstanceNotFoundException {


        TimeFrameEnum time = params.getInnerParams().getTimeFrame();
        LastFMData lastFMData = params.getInnerParams().getLastFMData();
        String artist = params.getInnerParams().getArtist();
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);

        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify, true, !params.getInnerParams().isNoredirect());
        BlockingQueue<UrlCapsule> queue = new DiscardableQueue<>(
                x -> !x.getArtistName().equalsIgnoreCase(scrobbledArtist.getArtist())
                , x -> x, 1);
        String lastfm = lastFMData.getName();
        lastFM.getChart(lastfm,
                new CustomTimeFrame(time),
                1000,
                1,
                TopEntity.ARTIST,
                ChartUtil.getParser(new CustomTimeFrame(time), TopEntity.ARTIST, ChartParameters.toListParams(), lastFM, lastfm),
                queue);
        List<UrlCapsule> objects = new ArrayList<>();
        queue.drainTo(objects);
        if (objects.isEmpty()) {
            sendMessageQueue(e, artist + " was not found on your top 1k artists" + time.getDisplayString() + ".");
            return;
        }
        UrlCapsule urlCapsule = objects.get(0);
        scrobbledArtist.setArtist(urlCapsule.getArtistName());
        int metricPlays = urlCapsule.getPlays();
        int artistPlays;
        if (time.equals(TimeFrameEnum.ALL)) {
            artistPlays = metricPlays;
        } else {
            artistPlays = lastFM.getArtistSummary(scrobbledArtist.getArtist(), lastfm).getUserPlayCount();
        }
        Long goal = params.getExtraParam();
        if (goal == null) {
            goal = (long) (Math.ceil(artistPlays / 1_000.) * 1_000);

        }
        final long unitNumber = 1;
        List<UserInfo> holder = lastFM.getUserInfo(List.of(lastfm));
        UserInfo mainUser = holder.get(0);
        int unixtimestamp = mainUser.getUnixtimestamp();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        long totalUnits;


        ZonedDateTime now = LocalDateTime.now().atZone(ZoneOffset.ofHours(2));
        int timestamp;
        ChronoUnit days = ChronoUnit.DAYS;
        switch (time) {
            case YEAR:
                timestamp = (int) now.minus(unitNumber, ChronoUnit.YEARS).toInstant().getEpochSecond();
                break;
            case QUARTER:
                timestamp = (int) now.minus(unitNumber * 4, ChronoUnit.MONTHS).toInstant().getEpochSecond();
                break;
            case MONTH:
                timestamp = (int) now.minus(unitNumber, ChronoUnit.MONTHS).toInstant().getEpochSecond();
                break;
            case ALL:
                timestamp = 0;
                break;
            case SEMESTER:
                timestamp = (int) now.minus(unitNumber * 2, ChronoUnit.MONTHS).toInstant().getEpochSecond();
                break;
            case WEEK:
                timestamp = (int) now.minus(unitNumber, ChronoUnit.WEEKS).toInstant().getEpochSecond();
                break;
            case DAY:
                timestamp = (int) now.minus(unitNumber, days).toInstant().getEpochSecond();
                break;
            default:
                throw new IllegalArgumentException();
        }
        timestamp = time.equals(TimeFrameEnum.ALL) ? unixtimestamp : timestamp;
        BiFunction<Temporal, Temporal, Long> between = days::between;
        LocalDateTime compareTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(1));
        totalUnits = between.apply(compareTime, now);
        double ratio = ((double) metricPlays) / totalUnits;
        double remainingUnits = (goal - artistPlays) / ratio;
        String userString = getUserString(e, lastFMData.getDiscordId(), lastfm);

        String timeFrame;
        if (time.equals(TimeFrameEnum.ALL)) timeFrame = " overall";
        else
            timeFrame = time.getDisplayString();
        String format = now.plus((long) remainingUnits, days).format(formatter);
        String unit = days.name().toLowerCase();
        String s = String.format("**%s** has a rate of **%s** scrobbles of **%s** per %s%s, so they are on pace to hit **%d** scrobbles by **%s**. (They have %d %s scrobbles)",
                userString, new DecimalFormat("#0.00").format(ratio), scrobbledArtist.getArtist(),
                unit.substring(0, unit.length() - 1),
                timeFrame, goal, format, artistPlays, scrobbledArtist.getArtist());

        sendMessageQueue(e, s);

    }
}
