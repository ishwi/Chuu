package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NumberParser;
import core.parsers.PaceParser;
import dao.ChuuService;
import dao.entities.NaturalTimeFrameEnum;
import dao.entities.UserInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Credits: to lfmwhoknows bot owner for the idea
 */
public class PaceCommand extends ConcurrentCommand {
    public PaceCommand(ChuuService dao) {
        super(dao);
        Map<Integer, String> map = new HashMap<>(1);
        map.put(NumberParser.INNER_ERROR, "The number introduced must be lower");
        map.put(NumberParser.LIMIT_ERROR, "You introduced a real big number");
        this.parser = new PaceParser(dao,
                map, " time units", "a");
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parse = parser.parse(e);
        if (parse == null) {
            return;
        }

        String lastfmId = parse[2];
        long discordId = Long.parseLong(parse[3]);
        String timeframe = parse[4];
        boolean useHours = Boolean.parseBoolean(parse[5]);
        List<UserInfo> holder = lastFM.getUserInfo(List.of(lastfmId));
        UserInfo mainUser = holder.get(0);
        int playCount = mainUser.getPlayCount();

        long unitNumber;
        long goal;
        if (parse[1].equals("null")) {
            if (parse[0].equals("null")) {
                // Both null we assume next 000 milestone and thats all
                unitNumber = 1;
                goal = (long) (Math.ceil(playCount / 10000.) * 10000);
            } else {
                // we only have one null
                long s = Long.parseLong(parse[0]);
                if (s < playCount) {
                    unitNumber = s;
                    goal = (long) (Math.ceil(playCount / 10000.) * 10000);
                } else {
                    goal = s;
                    unitNumber = 1;
                }
            }
        } else {
            unitNumber = Long.parseLong(parse[1]);
            goal = Long.parseLong(parse[0]);
        }

        NaturalTimeFrameEnum naturalTimeFrameEnum = NaturalTimeFrameEnum.fromCompletePeriod(timeframe);
        LocalDateTime now = LocalDateTime.now();
        int timestamp = switch (naturalTimeFrameEnum) {
            case YEAR -> (int) now.minus(unitNumber, ChronoUnit.YEARS).toInstant(ZoneOffset.UTC).getEpochSecond();
            case QUARTER -> (int) now.minus(unitNumber / 4, ChronoUnit.YEARS).toInstant(ZoneOffset.UTC).getEpochSecond();
            case MONTH -> (int) (int) now.minus(unitNumber, ChronoUnit.MONTHS).toInstant(ZoneOffset.UTC).getEpochSecond();
            case ALL -> 0;
            case SEMESTER -> (int) now.minus(unitNumber / 2, ChronoUnit.YEARS).toInstant(ZoneOffset.UTC).getEpochSecond();
            case WEEK -> (int) now.minus(unitNumber, ChronoUnit.WEEKS).toInstant(ZoneOffset.UTC).getEpochSecond();
            case DAY -> (int) (int) now.minus(unitNumber, ChronoUnit.DAYS).toInstant(ZoneOffset.UTC).getEpochSecond();
        };
        int totalScrobbles = lastFM.getInfoPeriod(lastfmId, timestamp);
        int unixtimestamp = mainUser.getUnixtimestamp();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        long totalUnits;
        if (totalScrobbles == playCount) {
            if (useHours)
                totalUnits = ChronoUnit.HOURS.between(LocalDateTime.ofEpochSecond(unixtimestamp, 0, ZoneOffset.UTC), now);
            else
                totalUnits = ChronoUnit.DAYS.between(LocalDateTime.ofEpochSecond(unixtimestamp, 0, ZoneOffset.UTC), now);

        } else {
            if (useHours)
                totalUnits = ChronoUnit.HOURS.between(LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC), now);
            else
                totalUnits = ChronoUnit.DAYS.between(LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC), now);
        }
        double ratio = ((double) totalScrobbles) / totalUnits;
        double remainingUnits = (goal - playCount) / ratio;
        String timeFrame = naturalTimeFrameEnum.equals(NaturalTimeFrameEnum.ALL) ? "overall" : ("over the last " + unitNumber + " " + (unitNumber == 1 ? naturalTimeFrameEnum : naturalTimeFrameEnum.toString().toLowerCase() + "s"));
        String format = now.plus((long) remainingUnits, useHours ? ChronoUnit.HOURS : ChronoUnit.DAYS).format(formatter);
        String userString = getUserString(discordId, e, lastfmId);
        String s = "**" + userString + "** has a rate of **" + new DecimalFormat("#0.00").format(ratio) + "** scrobbles per " + (useHours ? "hour" : "day") + " " + timeFrame + ", so they are on pace to hit **" + goal + "** scrobbles on **" + format + "**.";


        sendMessageQueue(e, s);
    }
}
