package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.EmbedUtils;
import main.commands.utils.TestResources;
import org.junit.Test;

import java.util.regex.Pattern;

public class AlbumCrownsCommandTest extends CommandTest {


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
		NullReturnParsersTest.twoUsersParser("!stolen");

	}


	@Test
	public void crowns() {

		String regex = "${header}'s crown(s)?";

		EmbedUtils.testLeaderboardEmbed("!crowns", EmbedUtils.descriptionArtistRegex, regex, false, Pattern
				.compile("You don't have any crown :'\\("));

		EmbedUtils.testLeaderboardEmbed("!crowns " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.descriptionArtistRegex, regex, false, true, Pattern
				.compile("You don't have any crown :'\\("));
	}

	@Test
	public void stolenCrowns() {

		Pattern noEmbeddedPatern = Pattern.compile("(.*) hasn't stolen anything from (.*)");

		Pattern noEmbeddedPatern2 = Pattern.compile("Sis, dont use the same person twice");

		String titleRegex = ".*?(?=Top 10 crowns Stolen by )Top 10 crowns Stolen by .*";
		TestResources.insertCommonArtistWithPlays(Integer.MAX_VALUE);
		EmbedUtils.testLeaderboardEmbed("!stolen " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.stolenRegex, titleRegex, false, false, noEmbeddedPatern);
		TestResources.insertCommonArtistWithPlays(1);
		EmbedUtils.testLeaderboardEmbed("!stolen " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.stolenRegex, titleRegex, false, false, noEmbeddedPatern);

		EmbedUtils.testLeaderboardEmbed("!stolen " + TestResources.testerJDA.getSelfUser()
				.getAsMention(), EmbedUtils.stolenRegex, titleRegex, false, false, noEmbeddedPatern2);

	}


	@Test
	public void crownsAlbum() {
		//Empty

		Pattern noembededMessage = Pattern.compile("(.*) doesn't have any album crown :'\\(");
		Pattern noembededMessageLb = Pattern.compile("This guild has no registered users:\\(");

		String regex = "${header}'s album crown(:?s)?";
		String regexLB = "${header}'s Album Crowns leadearboard";
		EmbedUtils.testLeaderboardEmbed("!crownsal " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.descriptionArtistAlbumRegex, regex, false, true, noembededMessage);
		EmbedUtils
				.testLeaderboardEmbed("!crownsalbumlb", EmbedUtils.descriptionArtistRegex, regexLB, true, noembededMessageLb);

		TestResources.dao
				.insertAlbumCrown(TestResources.commonArtist, "Test Album That shoudnt Exists", TestResources.testerJDA
						.getSelfUser().getIdLong(), TestResources.channelWorker.getGuild().getIdLong(), 199);
		//With something
		EmbedUtils
				.testLeaderboardEmbed("!crownsal", EmbedUtils.descriptionArtistAlbumRegex, regex, false, noembededMessage);
		EmbedUtils
				.testLeaderboardEmbed("!crownsalbumlb", EmbedUtils.descriptionArtistRegex, regexLB, true, noembededMessageLb);

		//Revert
		TestResources.dao
				.deleteAlbumCrown(TestResources.commonArtist, "Test Album That shoudnt Exists", TestResources.testerJDA
						.getSelfUser().getIdLong(), TestResources.channelWorker.getGuild().getIdLong());
	}

	@Test
	public void uniques() {

		Pattern noEmbeddPattern = Pattern.compile("You have no Unique Artists :\\(");
		String regex = "${header}'s Top 10 unique Artists";
		EmbedUtils.testLeaderboardEmbed("!unique", EmbedUtils.descriptionArtistRegex, regex, false, noEmbeddPattern);
		EmbedUtils.testLeaderboardEmbed("!unique " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.descriptionArtistRegex, regex, false, true, noEmbeddPattern);

	}

	@Test
	public void obscurityLb() {
		Pattern noembededMessageLb = Pattern.compile("This guild has no registered users:\\(");

		EmbedUtils
				.testLeaderboardEmbed("!obscuritylb", EmbedUtils.descriptionArtistRegex, "${header}'s Obscurity points leadearboard", true, noembededMessageLb);
	}


	@Test
	public void scrobbledLb() {
		Pattern noembededMessageLb = Pattern.compile("This guild has no registered users:\\(");

		EmbedUtils
				.testLeaderboardEmbed("!scrobbledlb", EmbedUtils.descriptionArtistRegex, "${header}'s artist leadearboard", true, noembededMessageLb);
	}

	@Test
	public void uniqueLb() {
		Pattern noembededMessageLb = Pattern.compile("This guild has no registered users:\\(");

		EmbedUtils
				.testLeaderboardEmbed("!uniquelb", EmbedUtils.descriptionArtistRegex, "${header}'s Unique Artists leadearboard", true, noembededMessageLb);
	}


}
