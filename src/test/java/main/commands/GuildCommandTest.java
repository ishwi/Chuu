package main.commands;

import main.commands.utils.CommandTest;
import main.commands.utils.ImageUtils;
import org.junit.Test;

public class GuildCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!guild";
	}

	@Override
	public void nullParserReturned() {
	}

	@Test
	public void normalUsage() {
		ImageUtils.testImage(COMMAND_ALIAS, 1500, 1500, ".png");
	}
}
