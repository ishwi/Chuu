package test.commands;

import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;
import org.junit.Test;

public class CountryCommandTest extends CommandTest {


	@Override
	public String giveCommandName() {
		return "!countries";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.timerFrameParser(COMMAND_ALIAS);

	}

	@Test
	public void countryTest() {
		ImageUtils
				.testImageWithPreWarning(COMMAND_ALIAS + " s", "Going to take a while", false, 1398, 2754, 75, ".png");

	}

	@Test
	public void countryTestWithMockedMb() {

	}
}
