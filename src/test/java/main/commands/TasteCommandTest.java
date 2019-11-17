package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.ImageUtils;
import main.commands.utils.OneLineUtils;
import main.commands.utils.TestResources;
import org.junit.Test;

import java.util.regex.Pattern;

public class TasteCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!taste";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.twoUsersParser(COMMAND_ALIAS);

	}

	@Test
	public void normalUsage() {
		TestResources.deleteCommonArtists();

		OneLineUtils.testCommands(COMMAND_ALIAS + " " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), Pattern.compile("You don't share any artist :\\("), null);
		TestResources.insertCommonArtistWithPlays(1);
		ImageUtils.testImage(COMMAND_ALIAS + " " + TestResources.ogJDA.getSelfUser().getAsMention(), 500, 600, ".png");

	}
}
