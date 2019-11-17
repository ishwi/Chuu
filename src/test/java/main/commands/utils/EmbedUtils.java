package main.commands.utils;

import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Before;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.commands.utils.TestResources.*;

public class EmbedUtils {
	public static Pattern descriptionArtistRegex = Pattern.compile(
			"(\\d+)" + //Indexed list *captured
					"\\. \\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //Markdown link
					"(?=(?: -|:))(?: -|:) " + //anything until a ":" or a " -"
					"(\\d+) " + //count of the description *captured
					"(play(?:s)?|(?:album )?crown(?:s)?|obscurity points|artist(?:s)?|unique artist(?:s)?)");
	//ending
	public static Pattern descriptionArtistRegexNoMarkDownLink = Pattern.compile(
			"(\\d+)" + //Indexed list *captured
					"\\. (?:.*) [-:] " + // aristName
					"(\\d+) " + //count of the description *captured
					"(play(?:s)?|(?:album )?crown(?:s)?|obscurity points|artist(?:s)?|unique artist(?:s)?)");
	public static Pattern stolenRegex = Pattern.compile(
			"(\\d+)" + //Indexed list *captured
					"\\. \\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //Markdown link
					"(?= : )(?: : )" + //anything until a ":"
					"(\\d+)" + //your plays
					"(?: -> )(?:\\d+)"); //Separator and other user plays
	public static Pattern descriptionArtistAlbumRegex = Pattern.compile(
			"(\\d+)\\. " + //digit
					"\\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //markdown url
					"(?= - ) - (\\d+) play(?:s)?"); /// remaining
	public static String serverThumbnail;
	public static String testerJDAThumbnail;
	public static String ogJDAThumbnail;
	public Function<String, String> getArtistThumbnail = (artistName) ->
			TestResources.dao.getArtistUrl(artistName);

	public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, String artistThumbnail, Pattern NoEmbededPattern) {
		testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, false, artistThumbnail, NoEmbededPattern);
	}

	public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, boolean hasPing, String artistThumbnail, Pattern NoEmbededPattern) {

		Predicate<Matcher> matcherBooleanFunction = (Matcher matcher) ->
				Long.parseLong(matcher.group(1)) >= 0 && Long.parseLong(matcher.group(2)) >= 0;

		testEmbed(command, descriptionRegex, matcherBooleanFunction, titleRegex, isLeaderboard, hasPing, artistThumbnail, NoEmbededPattern);
	}

	public static void testEmbed(String command, Pattern descriptionRegex, Predicate<Matcher> matcherDescription, String titleRegex, boolean isLeaderboard, boolean hasPing, String artistThumbnail, Pattern NoEmbededPattern) {
		String header;
		Optional<Member> first;
		if (hasPing) {
			first = channelWorker.getMembers().stream()
					.filter(x -> command.contains(x.getAsMention())).findFirst();
		} else {
			first = channelWorker.getMembers().stream()
					.filter(x -> x.getId().equals(testerJDA.getSelfUser().getId())).findFirst();
		}
		Assert.assertTrue(first.isPresent());

		if (isLeaderboard) {
			header = first.get().getGuild().getName();

		} else {
			header = first.get().getEffectiveName();
		}
		if (artistThumbnail == null) {
			artistThumbnail = isLeaderboard ? serverThumbnail : first.get().getUser().getAvatarUrl();
		}
		Pattern footerRegex = Pattern
				.compile("(" + header + " has (?:(\\d+) ((?:album )?crown(s)?!|registered user(s)?|unique artist(s)?|)|stolen (\\d+) crown(?:s)? {2})|(.*) has stolen \\d+ crowns)!");

		Pattern titlePattern = Pattern.compile(titleRegex.replaceAll("\\$\\{header}", header));

		GenericEmbedMatcher
				.GeneralFunction(command, footerRegex, null, titlePattern, null, descriptionRegex, matcherDescription,
						NoEmbededPattern, null, 45, true, artistThumbnail, null);


	}


	public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, boolean hasPing, Pattern NoEmbededPattern) {
		testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, hasPing, null, NoEmbededPattern);
	}

	public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, Pattern NoEmbededPattern) {
		testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, false, null, NoEmbededPattern);
	}


	public static void testNonLeadearboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, Pattern NoEmbededPattern) {
		testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, false, null, NoEmbededPattern);
	}


	@Before
	public void setUp() throws Exception {
		testerJDAThumbnail = testerJDA.getSelfUser().getAvatarUrl();
		ogJDAThumbnail = ogJDA.getSelfUser().getAvatarUrl();
		serverThumbnail = channelWorker.getGuild().getIconUrl();
	}


}
