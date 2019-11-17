package main.commands;

import main.commands.utils.CommandTest;
import main.commands.utils.EmbedUtils;
import org.junit.Test;

import java.util.regex.Pattern;

public class PlayingCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!playing";
	}

	@Override
	public void nullParserReturned() {
	}

	@Test
	public void normalUsage() {

		Pattern noEmbeddedMessage = Pattern.compile("None is listening to music at the moment UwU");
		EmbedUtils.testEmbed(COMMAND_ALIAS, Pattern
				.compile("\\+ (.*):(.*)-(.*)\\|(.*)"), null, "What is being played now in ${header}", true, false, null, noEmbeddedMessage);

		EmbedUtils.testEmbed(COMMAND_ALIAS + " --recent", Pattern
				.compile("\\+ (.*):(.*)-(.*)\\|(.*)"), null, "What is being played now in ${header}", true, false, null, noEmbeddedMessage);

	}
}
