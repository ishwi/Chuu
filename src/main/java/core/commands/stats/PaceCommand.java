package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.PaceParser;
import core.parsers.Parser;
import core.parsers.params.NaturalTimeParams;
import core.parsers.params.NumberParameters;
import core.services.UserInfoService;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.NaturalTimeFrameEnum;
import dao.entities.UserInfo;
import net.dv8tion.jda.api.utils.TimeFormat;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static core.parsers.ExtraParser.INNER_ERROR;
import static core.parsers.ExtraParser.LIMIT_ERROR;

/**
 * Credits: to lfmwhoknows bot owner for the idea
 */
public class PaceCommand extends ConcurrentCommand<NumberParameters<NumberParameters<NaturalTimeParams>>> {
    public PaceCommand(ServiceView dao) {
        super(dao);

    }

    public static int getTimestamp(NaturalTimeFrameEnum naturalTimeFrameEnum, UserInfo mainUser, long unitNumber, ZonedDateTime now) {
        return switch (naturalTimeFrameEnum) {
            case YEAR -> (int) now.minus(unitNumber, ChronoUnit.YEARS).toEpochSecond();
            case QUARTER -> (int) now.minus(unitNumber * 3, ChronoUnit.MONTHS).toEpochSecond();
            case MONTH -> (int) now.minus(unitNumber, ChronoUnit.MONTHS).toEpochSecond();
            case ALL -> mainUser.getUnixtimestamp();
            case SEMESTER -> (int) now.minus(unitNumber * 6, ChronoUnit.MONTHS).toEpochSecond();
            case WEEK -> (int) now.minus(unitNumber, ChronoUnit.WEEKS).toEpochSecond();
            case DAY -> (int) now.minus(unitNumber, ChronoUnit.DAYS).toEpochSecond();
            case HOUR -> (int) now.minus(unitNumber, ChronoUnit.HOURS).toEpochSecond();
            case MINUTE -> (int) now.minus(unitNumber, ChronoUnit.MINUTES).toEpochSecond();
            case SECOND -> (int) now.minus(unitNumber, ChronoUnit.SECONDS).toEpochSecond();
        };
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    //XD
    public Parser<NumberParameters<NumberParameters<NaturalTimeParams>>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(INNER_ERROR, "The number introduced must be lower");
        map.put(LIMIT_ERROR, "You introduced a real big number");
        return new PaceParser(db, map);
    }

    @Override
    public String getDescription() {
        return "Pace";
    }

    @Override
    public List<String> getAliases() {
        return List.of("pace");
    }

    @Override
    public String getName() {
        return "Pace";
    }

    @Override
    public void onCommand(Context e, @Nonnull NumberParameters<NumberParameters<NaturalTimeParams>> hopefullyNoOneSeesThis) throws LastFmException {


        NumberParameters<NaturalTimeParams> unitCount = hopefullyNoOneSeesThis.getInnerParams();
        NaturalTimeParams naturalTimeParams = unitCount.getInnerParams();
        LastFMData user = naturalTimeParams.getLastFMData();
        String lastfmId = user.getName();
        long discordId = user.getDiscordId();
        NaturalTimeFrameEnum naturalTimeFrameEnum = naturalTimeParams.getTime();


        UserInfo mainUser = new UserInfoService(db).refreshUserInfo(user);
        int playCount = mainUser.getPlayCount();
        String userString = getUserString(e, discordId, lastfmId);
        long unitNumber;
        long goal;
        Long tempU = hopefullyNoOneSeesThis.getExtraParam();
        Long tempS = unitCount.getExtraParam();

        if (tempU == null) {
            if (tempS == null) {
                // Both null we assume next 000 milestone and thats all
                unitNumber = 1;
                goal = (long) (Math.ceil(playCount / 10_000.) * 10_000);
            } else {
                // we only have one null
                long s = tempS;
                if (s <= playCount) {
                    unitNumber = s;
                    goal = (long) (Math.ceil(playCount / 10_000.) * 10_000);
                } else {
                    goal = s;
                    unitNumber = 1;
                }
            }
        } else {
            // We only have tempU
            if (tempS == null) {
                if (tempU <= playCount) {
                    unitNumber = tempU;
                    goal = (long) (Math.ceil(playCount / 10_000.) * 10_000);
                } else {
                    goal = tempU;
                    unitNumber = 1;
                }
            } else {
                unitNumber = Math.min(tempU, tempS);
                goal = Math.max(tempU, tempS);
            }
        }

        // UTC was not working with last.fm smh

        ZonedDateTime now = LocalDateTime.now().atZone(ZoneOffset.ofHours(2));
        int timestamp = getTimestamp(naturalTimeFrameEnum, mainUser, unitNumber, now);
        int totalScrobbles = lastFM.getInfoPeriod(user, timestamp);
        if (totalScrobbles == 0) {
            sendMessageQueue(e, userString + " hasn't played anything in the last " + unitNumber + " " + naturalTimeFrameEnum.toString().toLowerCase());
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        long totalUnits;

        totalUnits = 0;
        int i = -1;
        List<ChronoUnit> chronoUnits = List.of(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS);
        Map<ChronoUnit, BiFunction<Temporal, Temporal, Long>> functions = Map.of(ChronoUnit.DAYS, ChronoUnit.DAYS::between, ChronoUnit.HOURS, ChronoUnit.HOURS::between, ChronoUnit.MINUTES, ChronoUnit.MINUTES::between, ChronoUnit.SECONDS, ChronoUnit.SECONDS::between);
        LocalDateTime compareTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(1));
        while (totalUnits == 0) {
            i++;
            if (i == chronoUnits.size()) {
                sendMessageQueue(e, "Couldn't calculate your pace");
                return;
            }
            totalUnits = functions.get(chronoUnits.get(i)).apply(compareTime, now);

        }
        double ratio = ((double) totalScrobbles) / totalUnits;
        double remainingUnits = (goal - playCount) / ratio;
        String timeFrame;
        if (naturalTimeFrameEnum.equals(NaturalTimeFrameEnum.ALL)) timeFrame = "overall";
        else
            timeFrame = "over the last" + (unitNumber == 1 ? "" : " " + unitNumber) + " " + (unitNumber == 1 ? naturalTimeFrameEnum.toString().toLowerCase() : naturalTimeFrameEnum.toString().toLowerCase() + "s");
        ZonedDateTime target = now.plus((long) remainingUnits, chronoUnits.get(i));
        Instant instant = target.toInstant();
        String format = CommandUtil.getDateTimestampt(instant, TimeFormat.DATE_LONG);
        String unit = chronoUnits.get(i).name().toLowerCase();
        String s = String.format("**%s** has a rate of **%s** scrobbles per %s %s, so they are on pace to hit **%d** scrobbles by **%s**. (They currently have %d scrobbles)",
                userString,
                new DecimalFormat("#0.00").format(ratio),
                unit.substring(0, unit.length() - 1),
                timeFrame,
                goal,
                format,
                playCount);

        sendMessageQueue(e, s);
    }
}
