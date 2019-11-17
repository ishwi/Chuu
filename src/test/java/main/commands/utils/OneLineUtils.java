package main.commands.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Assert;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.commands.utils.TestResources.channelWorker;
import static org.awaitility.Awaitility.await;

public class OneLineUtils {
	public static void testCommands(String command, Pattern regex) {
		testCommands(command, regex, null);
	}

	public static void testCommands(String command, Pattern regex, Predicate<Matcher> function) {
		long id = channelWorker.sendMessage(command).complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		Matcher matcher = regex.matcher(message.getContentStripped());
		Assert.assertTrue(matcher.matches());
		if (function != null) {
			Assert.assertTrue(function.test(matcher));
		}
	}
}
