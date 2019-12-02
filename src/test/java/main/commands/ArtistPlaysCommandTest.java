package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.commands.utils.TestResources.channelWorker;
import static org.awaitility.Awaitility.await;

public class ArtistPlaysCommandTest extends CommandTest {

	private static final Pattern responsePattern = Pattern.compile(
			".+?(?=has scrobbled)has scrobbled ([\\w ]+) (\\d+) (times|time)");

	@Override
	public String giveCommandName() {
		return "!plays";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.artistParser(COMMAND_ALIAS);
	}

	@Test
	public void TestNormalPlays() {
		long id = channelWorker.sendMessage(COMMAND_ALIAS + " blackpink").complete().getIdLong();
		await().until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);

		Matcher matches = responsePattern.matcher(message.getContentStripped());
		Assert.assertTrue(matches.matches());
		String groupName = matches.group(1);
		int playCount = Integer.parseInt(matches.group(2));

		Assert.assertTrue(groupName.equalsIgnoreCase("BLACKPINK"));
		Assert.assertTrue(playCount >= 282);
		Assert.assertEquals("times", matches.group(3));
	}


	@Test
	public void TestOnePlay() {
		long id = channelWorker.sendMessage(COMMAND_ALIAS + " Will Philips").complete().getIdLong();
		await().until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);

		Matcher matches = responsePattern.matcher(message.getContentStripped());
		Assert.assertTrue(matches.matches());
		String groupName = matches.group(1);
		int playCount = Integer.parseInt(matches.group(2));

		Assert.assertEquals(groupName, "Will Philips");
		Assert.assertEquals(1, playCount);
		Assert.assertEquals("time", matches.group(3));
	}

}
