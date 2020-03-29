package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;
import test.commands.utils.OneLineUtils;

import java.util.regex.Pattern;

public class AlbumChartCommandTest extends CommandTest {

	@Override
	public String giveCommandName() {
		return "!chart";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.chartParser(COMMAND_ALIAS);
	}


	@Test
	public void ChartNormalTest() {

		//Equivalent partitions:
		// chart size >= 1x1 -> valid
		// chart size < 1x1 -> invalid

		//frontier values
		// 1x1 -> valid
		//0x0 -> invalid
		// 0x1 -> invalid
		// 1x0 -> invalid
		// MAX_INT x MAX_INT -> Valid but can take a bit of time


		ImageUtils.testImage(COMMAND_ALIAS + " a 1x1", 300, 300, ".png");
		Pattern errorPattern = Pattern.compile("Error on (?:.*)'s request:\n" +
				"0 is not a valid value for a chart!");

		OneLineUtils.testCommands(COMMAND_ALIAS + " a 0x0" ,errorPattern);
		OneLineUtils.testCommands(COMMAND_ALIAS + " a 0x1" ,errorPattern);
		OneLineUtils.testCommands(COMMAND_ALIAS + " a 1x0" ,errorPattern);

	}

	@Test
	public void ChartBigTest() {
		ImageUtils.testImage(COMMAND_ALIAS + " a 10x6", true, 900, 1500, 70, ".jpg");
	}

	@Test
	public void ChartOptionalsTest() {
		ImageUtils.testImage(COMMAND_ALIAS + " a 1x1 --notitles --plays", 300, 300, ".png");
	}

	@Test
	public void ChartBigWithWarningTest() {
		ImageUtils
				.testImageWithPreWarning(COMMAND_ALIAS + " a 101x1", "Going to take a while", true, 300, 300 * 101, ".png", ".jpg");
	}


}
