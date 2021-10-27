package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFMNoPlaysException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.TimeFrameEnum;
import dao.entities.Track;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DailyCommand extends ConcurrentCommand<ChuuDataParams> {
    public DailyCommand(ServiceView dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
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
    public void onCommand(Context e, @Nonnull ChuuDataParams params) throws LastFmException {

        LastFMData user = params.getLastFMData();
        String lastFmName = user.getName();
        long discordId = user.getDiscordId();
        String usable = getUserString(e, discordId, lastFmName);

        try {
            Map<Track, Integer> durationsFromWeek = lastFM.getTrackDurations(user, TimeFrameEnum.WEEK);
            SecondsTimeFrameCount minutesWastedOnMusicDaily = lastFM
                    .getMinutesWastedOnMusicDaily(user, durationsFromWeek,
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
