package test.commands;

import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;
import org.junit.Test;

public class ArtistChartCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!charta";
	}

	@Override
	public void nullParserReturned() {
		//Since its a child of chart, we already have tested it;
	}

	@Test
	public void ChartOptionalsTest() {

		ImageUtils.testImage(COMMAND_ALIAS + " a 1x1 --notitles --plays", 300, 300, ".png");
	}

}
