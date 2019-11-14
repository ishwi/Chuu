package main.commands;

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

import static main.commands.TestResources.*;
import static org.awaitility.Awaitility.await;

public class TestUtils {
	private static Pattern footerRegex;
	private static Pattern titleRegex;
	private static Pattern descriptionArtistRegex;

	private static Pattern descriptionArtistAlbumRegex;

	public static void testEmbeded(String command, Pattern descriptionRegex, boolean isLeaderboard) {
		testEmbeded(command, descriptionRegex, isLeaderboard, false);
	}

	public static void testEmbeded(String command, Pattern descriptionRegex, boolean isLeaderboard, boolean hasPing) {
		String header;

		Optional<Member> first = channelWorker.getMembers().stream()
				.filter(x -> x.getId().equals(
						(hasPing ? ogJDA : testerJDA)
								.getSelfUser().getId())).findFirst();
		Assert.assertTrue(first.isPresent());

		if (isLeaderboard) {
			header = first.get().getGuild().getName();
		} else {

			header = first.get().getEffectiveName();

		}
		footerRegex = Pattern
				.compile(header + " has (?:(\\d+) (crown(s)?!|registered user(s)?|unique artist(s)?|)|stolen (\\d+) crown(?:s)? {2})!");

		titleRegex = Pattern
				.compile(header + "'s (crown(s)?|Top 10 unique Artists|Crowns leadearboard|Unique Artists leadearboard|artist leadearboard|Obscurity points leadearboard|Top 10 crowns Stolen by .*)");

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
			Assert.assertNotNull(footer);
			Matcher matcher = footerRegex.matcher(footer.getText());
			Assert.assertTrue(matcher.matches());
			String footerTotalPlays = matcher.group(1);

			Matcher matcherTitle = titleRegex.matcher(messageEmbed.getTitle().replaceAll("\\*", ""));
			Assert.assertTrue(matcher.matches());

			String description = messageEmbed.getDescription();
			assert description != null;
			description = description.replaceAll("\\*", "");
			String[] split = description.split("\n");
			long max = Long.MAX_VALUE;
			int count = 1;
			for (String s : split) {

				Matcher matcherLine = descriptionRegex.matcher(s);
				Assert.assertTrue(matcherLine.matches());
				long local = Long.parseLong(matcherLine.group(2));
				long index = Long.parseLong(matcherLine.group(1));
				Assert.assertTrue(local <= max);
				Assert.assertEquals(index, count++);

				max = local;
			}

			if (messageEmbed.getThumbnail() != null) {
				if (isLeaderboard)
					Assert.assertEquals(messageEmbed.getThumbnail().getUrl(), channelWorker.getGuild().getIconUrl());
				else
					Assert.assertEquals(messageEmbed.getThumbnail().getUrl(), first.get().getUser().getAvatarUrl());
			}
			message.addReaction("U+27A1").submit();
			message.addReaction("U+27A1").submit();
		} else {
			Assert.assertTrue(Arrays
					.asList("You don't have any crown :'(", "This guild has no registered users:(", header + " doesn't have any album crown :'(")
					.contains(message.getContentStripped()));
		}
	}
}
