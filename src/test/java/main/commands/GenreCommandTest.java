package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.ImageUtils;
import org.junit.Test;

public class GenreCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!genre";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.timerFrameParser(COMMAND_ALIAS);
	}

	@Test
	public void normalUsage() {
		ImageUtils.testImage(COMMAND_ALIAS + " s", false, 600, 800, 40, ".png");

	}
}
