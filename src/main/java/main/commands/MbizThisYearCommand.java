package main.commands;

import dao.DaoImplementation;
import dao.entities.TimeFrameEnum;
import main.exceptions.InstanceNotFoundException;
import main.exceptions.LastFmException;
import main.parsers.ChartFromYearVariableParser;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Arrays;
import java.util.List;

public class MbizThisYearCommand extends MusicBrainzCommand {


	public MbizThisYearCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new ChartFromYearVariableParser(dao);
	}

	@Override
	public boolean doDiscogs() {
		return false;
	}


	@Override
	public String getDescription() {
		return "Gets your top albums of the year queried.\t" +
				"NOTE: The further the year is from the  current year, the less precise the command will be";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("aoty", "albumoftheyear");
	}

	@Override
	public String getName() {
		return "Your chart with your top albums of the year!";
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;

		int x = Integer.parseInt(returned[0]);
		int y = Integer.parseInt(returned[1]);
		Year year = Year.of(Integer.parseInt(returned[2]));
		String username = returned[3];
		boolean titleWrite = !Boolean.parseBoolean(returned[4]);
		boolean playsWrite = Boolean.parseBoolean(returned[5]);
		boolean caresAboutSize = !Boolean.parseBoolean(returned[6]);


		TimeFrameEnum timeframe;
		LocalDateTime time = LocalDateTime.now();
		if (year.isBefore(Year.of(time.getYear()))) {
			timeframe = TimeFrameEnum.ALL;
		} else {
			int monthValue = time.getMonthValue();
			if (monthValue == 1 && time.getDayOfMonth() < 8) {
				timeframe = TimeFrameEnum.WEEK;
			} else if (monthValue < 2) {
				timeframe = TimeFrameEnum.MONTH;
			} else if (monthValue < 4) {
				timeframe = TimeFrameEnum.QUARTER;
			} else if (monthValue < 7)
				timeframe = TimeFrameEnum.SEMESTER;
			else {
				timeframe = TimeFrameEnum.YEAR;
			}
		}
		Message will_take_a_while = sendMessage(e, "Will take a while").complete();
		calculateYearAlbums(username, timeframe
				.toApiFormat(), 1500, x, y, year, e, titleWrite, playsWrite, caresAboutSize);
		will_take_a_while.delete().queue();
	}


}
