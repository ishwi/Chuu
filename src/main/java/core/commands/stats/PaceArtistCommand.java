package core.commands.stats;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.apis.last.queues.DiscardableQueue;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistTimeFrameParser;
import core.parsers.NumberParser;
import core.parsers.Parser;
import core.parsers.params.ArtistTimeFrameParameters;
import core.parsers.params.ChartParameters;
import core.parsers.params.NumberParameters;
import core.parsers.utils.CustomTimeFrame;
import core.services.UserInfoService;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.TimeFrameEnum;
import dao.entities.UserInfo;
import net.dv8tion.jda.api.utils.TimeFormat;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.time.Instant;
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


    public PaceArtistCommand(ServiceView dao) {
        super(dao);
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
        return new NumberParser<>(new ArtistTimeFrameParser(db, lastFM),
                null,
                Integer.MAX_VALUE,
                map, s, false, true, true, "goal");
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
    protected void onCommand(Context e, @NotNull NumberParameters<ArtistTimeFrameParameters> params) throws LastFmException {


        TimeFrameEnum time = params.getInnerParams().getTimeFrame();
        LastFMData user = params.getInnerParams().getLastFMData();
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getInnerParams().getArtist(), !params.getInnerParams().isNoredirect());
        String artist = params.getInnerParams().getArtist();
        BlockingQueue<UrlCapsule> queue = new DiscardableQueue<>(
                x -> !x.getArtistName().equalsIgnoreCase(sA.getArtist())
                , x -> x, 1);
        String lastfm = user.getName();
        lastFM.getChart(user,
                new CustomTimeFrame(time),
                1000,
                1,
                TopEntity.ARTIST,
                ChartUtil.getParser(new CustomTimeFrame(time), TopEntity.ARTIST, ChartParameters.toListParams(), lastFM, user),
                queue);
        List<UrlCapsule> objects = new ArrayList<>();
        queue.drainTo(objects);
        if (objects.isEmpty()) {
            sendMessageQueue(e, artist + " was not found on your top 1k artists" + time.getDisplayString() + ".");
            return;
        }
        UrlCapsule urlCapsule = objects.get(0);
        sA.setArtist(urlCapsule.getArtistName());
        int metricPlays = urlCapsule.getPlays();
        int artistPlays;
        if (time.equals(TimeFrameEnum.ALL)) {
            artistPlays = metricPlays;
        } else {
            artistPlays = lastFM.getArtistSummary(sA.getArtist(), user).userPlayCount();
        }
        Long goal = params.getExtraParam();
        if (goal == null) {
            goal = (long) (Math.ceil(artistPlays / 1_000.) * 1_000);

        }
        final long unitNumber = 1;
        UserInfo mainUser = new UserInfoService(db).refreshUserInfo(user);
        int unixtimestamp = mainUser.getUnixtimestamp();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        long totalUnits;


        ZonedDateTime now = LocalDateTime.now().atZone(ZoneOffset.ofHours(2));
        int timestamp;
        ChronoUnit days = ChronoUnit.DAYS;
        timestamp = switch (time) {
            case YEAR -> (int) now.minus(unitNumber, ChronoUnit.YEARS).toInstant().getEpochSecond();
            case QUARTER -> (int) now.minus(unitNumber * 3, ChronoUnit.MONTHS).toInstant().getEpochSecond();
            case MONTH -> (int) now.minus(unitNumber, ChronoUnit.MONTHS).toInstant().getEpochSecond();
            case ALL -> unixtimestamp;
            case SEMESTER -> (int) now.minus(unitNumber * 6, ChronoUnit.MONTHS).toInstant().getEpochSecond();
            case WEEK -> (int) now.minus(unitNumber, ChronoUnit.WEEKS).toInstant().getEpochSecond();
            case DAY -> (int) now.minus(unitNumber, days).toInstant().getEpochSecond();
        };
        BiFunction<Temporal, Temporal, Long> between = days::between;
        LocalDateTime compareTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(1));
        totalUnits = between.apply(compareTime, now);
        double ratio = ((double) metricPlays) / totalUnits;
        double remainingUnits = (goal - artistPlays) / ratio;
        String userString = getUserString(e, user.getDiscordId(), lastfm);

        String timeFrame;
        if (time.equals(TimeFrameEnum.ALL)) timeFrame = " overall";
        else
            timeFrame = time.getDisplayString();
        ZonedDateTime target = now.plus((long) remainingUnits, days);
        Instant instant = target.toInstant();
        String format = CommandUtil.getDateTimestampt(instant, TimeFormat.DATE_LONG);
        String unit = days.name().toLowerCase();
        String s = String.format("**%s** has a rate of **%s** scrobbles of **%s** per %s%s, so they are on pace to hit **%d** scrobbles by **%s**. (They have %d %s scrobbles)",
                userString, new DecimalFormat("#0.00").format(ratio), sA.getArtist(),
                unit.substring(0, unit.length() - 1),
                timeFrame, goal, format, artistPlays, sA.getArtist());

        sendMessageQueue(e, s);

    }
}
