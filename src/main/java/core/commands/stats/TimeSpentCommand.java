package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.TimeFrameParameters;
import dao.ServiceView;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.TimeFrameEnum;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TimeSpentCommand extends ConcurrentCommand<TimeFrameParameters> {
    public TimeSpentCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<TimeFrameParameters> initParser() {
        return new TimerFrameParser(db, TimeFrameEnum.WEEK);
    }

    @Override
    public String getDescription() {
        return "Minutes listened last week";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("minutes");
    }

    @Override
    protected void onCommand(Context e, @Nonnull TimeFrameParameters params) throws LastFmException {

        String username = params.getLastFMData().getName();
        long discordId = params.getLastFMData().getDiscordId();
        TimeFrameEnum timeframe = params.getTime();
        String usableString = getUserString(e, discordId, username);
        if (Stream.of(TimeFrameEnum.WEEK, TimeFrameEnum.DAY, TimeFrameEnum.MONTH, TimeFrameEnum.QUARTER).noneMatch(timeframe::equals)) {
            sendMessageQueue(e, "Only [d]ay, [w]eek,[m]onth and [q]uarter are supported at the moment, sorry :'(");
            return;
        }

        SecondsTimeFrameCount wastedOnMusic = lastFM.getMinutesWastedOnMusic(params.getLastFMData(), timeframe);
        sendMessageQueue(e, String.format("**%s** played %d minutes of music, %s%s), listening to %d different tracks in the last %s", usableString, wastedOnMusic.getMinutes(), String
                        .format("(%d:%02d ", wastedOnMusic.getHours(),
                                wastedOnMusic.getRemainingMinutes()),
                CommandUtil.singlePlural(wastedOnMusic.getHours(), "hour", "hours"), wastedOnMusic.getCount(), wastedOnMusic.getTimeFrame().toString()
                        .toLowerCase()));

    }

    @Override
    public String getName() {
        return "Wasted On Music";
    }
}
