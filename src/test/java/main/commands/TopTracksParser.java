package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.EmbedUtils;
import org.junit.Test;

public class TopTracksParser extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!tt";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.timerFrameParser(COMMAND_ALIAS);
	}

	@Test
	public void normalExample() {

		EmbedUtils
				.testLeaderboardEmbed(COMMAND_ALIAS, EmbedUtils.descriptionArtistAlbumRegex, "${header}'s top  tracks in .*?", false, false);
	}
}
