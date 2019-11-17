package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.ImageUtils;
import org.junit.Test;

public class ChartCommandTest extends CommandTest {

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
		ImageUtils.testImage(COMMAND_ALIAS + " a 1x1", 300, 300, ".png");
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
