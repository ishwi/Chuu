package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import dao.ChuuService;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DailyCommand extends ConcurrentCommand {
    public DailyCommand(ChuuService dao) {
        super(dao);
        parser = new OnlyUsernameParser(dao);
    }

    @Override
    public String getDescription() {
        return "Return time spent listening in the last 24 hours";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("daily", "day");
    }

    @Override
    public String getName() {
        return "Daily";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned = parser.parse(e);
        String lastFmName = returned[0];
        long discordID = Long.parseLong(returned[1]);
        String usable = getUserString(e, discordID, lastFmName);

        try {
            Map<Track, Integer> durationsFromWeek = lastFM.getTrackDurations(lastFmName, TimeFrameEnum.WEEK);
            SecondsTimeFrameCount minutesWastedOnMusicDaily = lastFM
                    .getMinutesWastedOnMusicDaily(lastFmName, durationsFromWeek,
                            (int) Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond());

            sendMessageQueue(e, MessageFormat.format("**{0}** played {1} minutes of music, {2}hours), listening to {3}{4} in the last 24 hours", usable, minutesWastedOnMusicDaily.getMinutes(), String
                    .format("(%d:%02d ", minutesWastedOnMusicDaily.getHours(),
                            minutesWastedOnMusicDaily.getRemainingMinutes()), minutesWastedOnMusicDaily
                    .getCount(), CommandUtil.singlePlural(minutesWastedOnMusicDaily.getCount(),
                    " track", " tracks")));

        } catch (LastFMNoPlaysException ex) {
            sendMessageQueue(e, "**" + usable + "** played 0 mins, really, 0! mins in the last 24 hours");
        }
    }
}
