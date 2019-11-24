package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.ImageUtils;
import main.commands.utils.OneLineUtils;
import org.junit.Test;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackListCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!tracks";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.artistAlbumParser(COMMAND_ALIAS);
	}

	@Test
	public void normalFunctionallity() {
		ImageUtils.testImage(COMMAND_ALIAS + " Blackpink - Square Up", true, Integer.MAX_VALUE, 935, ".png");
	}

	@Test
	public void OneMusicBrainzSearch() {
		ImageUtils.testImage(COMMAND_ALIAS + " LOOΠΔ - [x x]", true, Integer.MAX_VALUE, 935, ".png");
	}

	@Test
	public void NoResults() {
		Pattern pattern = Pattern.compile("Couldn't find a tracklist for ([^-]+) - (.*)");
		Predicate<Matcher> predicate = matcher1 ->
				matcher1.group(1).equalsIgnoreCase("れをる") &&
						matcher1.group(2).equalsIgnoreCase("No title−");
		OneLineUtils
				.testCommands(COMMAND_ALIAS + " れをる - No title−", pattern, predicate);
	}

	@Test
	public void TwoMusicBrainzSearch() {
		ImageUtils.testImage(COMMAND_ALIAS + " LOOΠΔ - [+ +]", true, Integer.MAX_VALUE, 935, ".png");
	}
}
