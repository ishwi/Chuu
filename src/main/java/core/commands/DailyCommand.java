package core.commands;

import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DailyCommand extends ConcurrentCommand<ChuuDataParams> {
    public DailyCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(getService());
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
    void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {

        String lastFmName = params.getLastFMData().getName();
        Long discordId = params.getLastFMData().getDiscordId();
        String usable = getUserString(e, discordId, lastFmName);

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
