package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.EmbedUtils;
import main.commands.utils.TestResources;
import org.junit.Test;

import java.util.regex.Pattern;

public class FavesCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!favs";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.artistTimeFrameParser(COMMAND_ALIAS);
	}

	@Test
	public void normalFunctionallity() {
		String blacpink = TestResources.dao.getArtistUrl("BLACKPINK");
		EmbedUtils
				.testLeaderboardEmbed(COMMAND_ALIAS + " BLACKPINK", EmbedUtils.descriptionArtistRegexNoMarkDownLink, "${header}'s Top (.*) Tracks in (.*)",
						false, false, blacpink, Pattern.compile("No faves on provided time!"));

		String url2 = TestResources.dao.getArtistUrl("My Bloody Valentine");

		EmbedUtils
				.testLeaderboardEmbed(COMMAND_ALIAS + " My Bloody Valentine" + " w", EmbedUtils.descriptionArtistRegexNoMarkDownLink, "${header}'s Top (.*) Tracks in (.*)",
						false, false, url2, Pattern.compile("No faves on provided time!"));
	}
}

