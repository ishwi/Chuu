package test.commands;

import org.junit.Test;
import test.commands.utils.CommandTest;
import test.commands.utils.OneLineUtils;

import java.util.regex.Pattern;

public class SourceCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!source";
	}

	@Override
	public void nullParserReturned() {

	}

	@Test
	public void normalFunctionality() {
		Pattern compile = Pattern.compile("This is the GitHub link of the bot:\nhttps://github.com/(.*)");
		OneLineUtils.testCommands(COMMAND_ALIAS, compile);
	}
}
