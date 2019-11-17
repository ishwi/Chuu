package main.commands.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.commands.utils.TestResources.channelWorker;
import static main.commands.utils.TestResources.testerJDA;
import static org.awaitility.Awaitility.await;

public class EmbedUtils {
	public static Pattern descriptionArtistRegex = Pattern.compile(
			"(\\d+)" + //Indexed list *captured
					"\\. \\[(?:[^\\[\\]]+)]\\((?:[^)]+)\\)" + //Markdown link
					"(?=(?: -|:))(?: -|:) " + //anything until a ":" or a " -"
					"(\\d+) " + //count of the description *captured
					"(play(?:s)?|(?:album )?crown(?:s)?|obscurity points|artist(?:s)?|unique artist(?:s)?)"); //ending
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

	public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard) {
		testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, false, false, null);
	}

	private static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, boolean hasPing, boolean hasArtistThumbnail, String artistThumbnail) {
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

		Pattern footerRegex = Pattern
				.compile(header + " has (?:(\\d+) ((?:album )?crown(s)?!|registered user(s)?|unique artist(s)?|)|stolen (\\d+) crown(?:s)? {2})!");

		long id = channelWorker.sendMessage(command).complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;

		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);

		if (!message.getEmbeds().isEmpty()) {

			MessageEmbed messageEmbed = message.getEmbeds().get(0);
			MessageEmbed.Footer footer = messageEmbed.getFooter();
			if (footer != null) {
				Matcher matcher = footerRegex.matcher(footer.getText());
				Assert.assertTrue(matcher.matches());
				String footerTotalPlays = matcher.group(1);
			}
			String title = messageEmbed.getTitle().replaceAll("\\*", "");
			Pattern titlePattern = Pattern.compile(titleRegex.replaceAll("\\$\\{header}", header));
			Matcher matcherTitle = titlePattern.matcher(title);
			Assert.assertTrue(matcherTitle.matches());

			String description = messageEmbed.getDescription();
			assert description != null;
			description = description.replaceAll("\\*", "");
			String[] split = description.split("\n");
			long max = Long.MAX_VALUE;
			int count = 1;
			for (String s : split) {

				Matcher matcherLine = descriptionRegex.matcher(s);
				Assert.assertTrue(matcherLine.matches());
				try {
					long local = Long.parseLong(matcherLine.group(2));
					long index = Long.parseLong(matcherLine.group(1));
					Assert.assertTrue(local <= max);
					Assert.assertEquals(index, count++);

					max = local;
				} catch (NumberFormatException ignored) {
					//This is not a leaderboard
					//TODO Cleaner way to avoid this
				}
			}

			if (messageEmbed.getThumbnail() != null) {
				if (hasArtistThumbnail) {
					Assert.assertEquals(messageEmbed.getThumbnail().getUrl(), artistThumbnail);
				} else {
					if (isLeaderboard)
						Assert.assertEquals(messageEmbed.getThumbnail().getUrl(), channelWorker.getGuild()
								.getIconUrl());
					else
						Assert.assertEquals(messageEmbed.getThumbnail().getUrl(), first.get().getUser().getAvatarUrl());
				}
			}
			message.addReaction("U+27A1").submit();
			message.addReaction("U+27A1").submit();
		} else {
			Assert.assertTrue(Arrays
					.asList("You don't have any crown :'(",
							"This guild has no registered users:(",
							header + " doesn't have any album crown :'(",
							"You have no Unique Artists :(",
							"Sis, dont use the same person twice"
					)
					.contains(message.getContentStripped()) || message.getContentStripped()
					.contains("hasn't stolen anything from"));
		}
	}

	public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, boolean hasPing) {
		testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, hasPing, false, null);
	}

	public static void testLeaderboardEmbed(String command, Pattern descriptionRegex, String titleRegex, boolean isLeaderboard, boolean hasPing, String artistThumbnail) {
		testLeaderboardEmbed(command, descriptionRegex, titleRegex, isLeaderboard, false, true, artistThumbnail);
	}
}
