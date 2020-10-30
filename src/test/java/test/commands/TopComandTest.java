package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;

public class TopComandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!top";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.topParser(COMMAND_ALIAS);
	}

	@Test
	public void normalUsage() {
		ImageUtils.testImage(COMMAND_ALIAS, 1500, 1500, ".png");
		ImageUtils.testImage(COMMAND_ALIAS + " --artist", 1500, 1500, ".png");
	}
}
