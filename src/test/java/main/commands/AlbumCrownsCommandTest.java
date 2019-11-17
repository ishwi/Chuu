package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.EmbedUtils;
import main.commands.utils.TestResources;
import org.junit.Test;

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

		EmbedUtils.testLeaderboardEmbed("!crowns", EmbedUtils.descriptionArtistRegex, regex, false);

		EmbedUtils.testLeaderboardEmbed("!crowns " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.descriptionArtistRegex, regex, false);
	}

	@Test
	public void stolenCrowns() {

		String titleRegex = ".*?(?=Top 10 crowns Stolen by )Top 10 crowns Stolen by .*";
		TestResources.insertCommonArtistWithPlays(Integer.MAX_VALUE);
		EmbedUtils.testLeaderboardEmbed("!stolen " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.stolenRegex, titleRegex, false, true);
		TestResources.insertCommonArtistWithPlays(1);
		EmbedUtils.testLeaderboardEmbed("!stolen " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.stolenRegex, titleRegex, false, true);

		EmbedUtils.testLeaderboardEmbed("!stolen " + TestResources.testerJDA.getSelfUser()
				.getAsMention(), EmbedUtils.stolenRegex, titleRegex, false, false);

	}


	@Test
	public void crownsAlbum() {
		//Empty
		String regex = "${header}'s album crown(:?s)?";
		String regexLB = "${header}'s Album Crowns leadearboard";
		EmbedUtils.testLeaderboardEmbed("!crownsal " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.descriptionArtistAlbumRegex, regex, false, true);
		EmbedUtils.testLeaderboardEmbed("!crownsalbumlb", EmbedUtils.descriptionArtistRegex, regexLB, true);

		TestResources.dao
				.insertAlbumCrown(TestResources.commonArtist, "Test Album That shoudnt Exists", TestResources.testerJDA
						.getSelfUser().getIdLong(), TestResources.channelWorker.getGuild().getIdLong(), 199);
		//With something
		EmbedUtils.testLeaderboardEmbed("!crownsal", EmbedUtils.descriptionArtistAlbumRegex, regex, false);
		EmbedUtils.testLeaderboardEmbed("!crownsalbumlb", EmbedUtils.descriptionArtistRegex, regexLB, true);

		//Revert
		TestResources.dao
				.deleteAlbumCrown(TestResources.commonArtist, "Test Album That shoudnt Exists", TestResources.testerJDA
						.getSelfUser().getIdLong(), TestResources.channelWorker.getGuild().getIdLong());
	}

	@Test
	public void uniques() {
		String regex = "${header}'s Top 10 unique Artists";
		EmbedUtils.testLeaderboardEmbed("!unique", EmbedUtils.descriptionArtistRegex, regex, false);
		EmbedUtils.testLeaderboardEmbed("!unique " + TestResources.ogJDA.getSelfUser()
				.getAsMention(), EmbedUtils.descriptionArtistRegex, regex, false, true);

	}

	@Test
	public void obscurityLb() {
		EmbedUtils
				.testLeaderboardEmbed("!obscuritylb", EmbedUtils.descriptionArtistRegex, "${header}'s Obscurity points leadearboard", true);
	}


	@Test
	public void scrobbledLb() {
		EmbedUtils
				.testLeaderboardEmbed("!scrobbledlb", EmbedUtils.descriptionArtistRegex, "${header}'s artist leadearboard", true);
	}

	@Test
	public void uniqueLb() {
		EmbedUtils
				.testLeaderboardEmbed("!uniquelb", EmbedUtils.descriptionArtistRegex, "${header}'s Unique Artists leadearboard", true);
	}


}
