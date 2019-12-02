package main.commands;

import dao.entities.LastFMData;
import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.ImageUtils;
import main.commands.utils.OneLineUtils;
import main.commands.utils.TestResources;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class MusicBrainzCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!releaseyear";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.chartFromYearParser(COMMAND_ALIAS);
	}

	@Test
	public void normalUsage() {
		ImageUtils.testImage(COMMAND_ALIAS, true, Integer.MAX_VALUE, Integer.MAX_VALUE, 60, ".png", ".jpg");

		Pattern compile = Pattern.compile("Dont have any (\\d{4}) album in your top (\\d+) albums");

		OneLineUtils.testCommands(COMMAND_ALIAS + " a 1876 ", compile, matcher -> Integer.parseInt(matcher.group(1)) == 1876 && Integer.parseInt(matcher.group(2)) == MusicBrainzCommand.chartSize, 65);


	}
}
