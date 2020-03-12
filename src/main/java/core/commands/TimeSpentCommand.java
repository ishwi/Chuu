package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.TimerFrameParser;
import dao.ChuuService;
import dao.entities.SecondsTimeFrameCount;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class TimeSpentCommand extends ConcurrentCommand {
    public TimeSpentCommand(ChuuService dao) {
        super(dao);
        this.parser = new TimerFrameParser(dao, TimeFrameEnum.WEEK);
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
        String[] message;
        message = parser.parse(e);
        String username = message[0];
        long discordId = Long.parseLong(message[1]);
        String timeframe = message[2];
        String usableString = getUserString(e, discordId, username);
        if (!timeframe.equals("7day") && !timeframe.equals("1month") && !timeframe.equals("3month")) {
            sendMessageQueue(e, "Only [w]eek,[m]onth and [q]uarter are supported at the moment , sorry :'(");
            return;
        }

        SecondsTimeFrameCount wastedOnMusic = lastFM.getMinutesWastedOnMusic(username, timeframe);
        sendMessageQueue(e, "**" + usableString + "** played " +
                            wastedOnMusic.getMinutes() +
                            " minutes of music, " + String
                                    .format("(%d:%02d ", wastedOnMusic.getHours(),
							wastedOnMusic.getRemainingMinutes()) +
					CommandUtil.singlePlural(wastedOnMusic.getHours(), "hour", "hours") +
					"), listening to " + wastedOnMusic.getCount() + " different tracks in the last " +
					wastedOnMusic.getTimeFrame().toString()
							.toLowerCase());

	}

	@Override
	public String getName() {
		return "Wasted On Music";
	}
}
