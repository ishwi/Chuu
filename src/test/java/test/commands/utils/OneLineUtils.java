package test.commands.utils;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.junit.Assert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.awaitility.Awaitility.await;
import static test.commands.utils.TestResources.channelWorker;

public class OneLineUtils {
	public static void testCommands(String command, Pattern regex) {
		testCommands(command, regex, null);
	}

	public static void testCommands(String command, Pattern regex, Predicate<Matcher> function) {
		testCommands(command, regex, function, 45);
	}


	public static void testCommands(String command, Pattern regex, Predicate<Matcher> function, int timeout) {
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

	public static void embedLink(String command, String imageUrl, Pattern regex, Predicate<Matcher> function, int timeout) {
		MessageAction messageAction;
		try {
			MessageBuilder messageBuilder = new MessageBuilder();
			URL url = new URL(imageUrl);
			BufferedImage file = ImageIO.read(url);
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ImageIO.write(file, "png", b);
			byte[] img = b.toByteArray();
			messageAction = messageBuilder.setContent(command).
					sendTo(channelWorker).addFile(img, "cat.png");
		} catch (IOException e) {
			Assert.fail();
			return;
		}

		long id = messageAction.complete().getIdLong();
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
