package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import org.junit.Test;

import java.util.regex.Pattern;

public class AlbumCrownsCommandTest extends CommandTest {
	public Pattern descriptionArtistRegex = Pattern.compile(
			"(\\d+)" + //Indexed list *captured
					"\\. \\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //Markdown link
					"(?=(?: -|:))(?: -|:) " + //anything until a ":" or a " -"
					"(\\d+) " + //count of the description *captured
					"(play(?:s)?|crown(?:s)?|obscurity points|artist(?:s)?|unique artist(?:s)?)"); //ending
	public Pattern stolenRegex = Pattern.compile(
			"(\\d+)" + //Indexed list *captured
					"\\. \\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //Markdown link
					"(?= : )(?: : )" + //anything until a ":"
					"(\\d+)" + //your plays
					"(?: -> )(?:\\d+)"); //Separator and other user plays
	public Pattern descriptionArtistAlbumRegex = Pattern.compile(
			"(\\d+)\\. " + //digit
					"\\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //markdown url
					"(?= - ) - (\\d+) play(?:s)?"); /// remaining


	@Override
	public String giveCommandName() {
		return null;
	}

	//No Parsers
	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.onlyUsernameParser("!crowns");
		NullReturnParsersTest.onlyUsernameParser("!crownsal");
		NullReturnParsersTest.onlyUsernameParser("!unique");

	}


	@Test
	public void crowns() {
		TestUtils.testEmbeded("!crowns", descriptionArtistRegex, false);
	}

	@Test
	public void stolenCrowns() {
		TestUtils.testEmbeded("!stolen " + TestResources.ogJDA.getSelfUser().getAsMention(), stolenRegex, false, true);
	}


	@Test
	public void crownsAlbum() {
		TestUtils.testEmbeded("!crownsal", descriptionArtistAlbumRegex, false);
	}

	@Test
	public void uniques() {
		TestUtils.testEmbeded("!unique", descriptionArtistRegex, false);
	}

	@Test
	public void obscurityLb() {
		TestUtils.testEmbeded("!obscuritylb", descriptionArtistRegex, true);
	}

	@Test
	public void crownsalbumLB() {
		TestUtils.testEmbeded("!crownsalbumlb", descriptionArtistRegex, true);
	}

	@Test
	public void scrobbledLb() {
		TestUtils.testEmbeded("!scrobbledlb", descriptionArtistRegex, true);
	}

	@Test
	public void uniqueLb() {
		TestUtils.testEmbeded("!uniquelb", descriptionArtistRegex, true);
	}
}
