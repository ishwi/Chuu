package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TimerFrameParser;
import core.parsers.params.TimeFrameParameters;
import dao.ChuuService;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TimeSpentCommand extends ConcurrentCommand<TimeFrameParameters> {
    public TimeSpentCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<TimeFrameParameters> getParser() {
        return new TimerFrameParser(getService(), TimeFrameEnum.WEEK);
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
    protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        TimeFrameParameters params = parser.parse(e);
        String username = params.getLastFMData().getName();
        long discordId = params.getLastFMData().getDiscordId();
        TimeFrameEnum timeframe = params.getTime();
        String usableString = getUserString(e, discordId, username);
        if (Stream.of(TimeFrameEnum.WEEK, TimeFrameEnum.DAY, TimeFrameEnum.MONTH, TimeFrameEnum.QUARTER).noneMatch(timeframe::equals)) {
            sendMessageQueue(e, "Only [d]ay, [w]eek,[m]onth and [q]uarter are supported at the moment, sorry :'(");
            return;
        }

        SecondsTimeFrameCount wastedOnMusic = lastFM.getMinutesWastedOnMusic(username, timeframe.toApiFormat());
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
