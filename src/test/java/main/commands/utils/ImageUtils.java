package main.commands.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static main.commands.utils.TestResources.channelWorker;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class ImageUtils {

	public static void testImage(String command, int height, int width, String... formats) {
		testImage(command, false, height, width, 45, formats);
	}

	public static void testImage(String command, boolean isSizeLimit, int height, int width, int timeout, String... formats) {
		long id = channelWorker.sendMessage(command).complete().getIdLong();
		await().atMost(timeout, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});

		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		batteryTestForImage(message, isSizeLimit, height, width, formats);
	}

	private static void batteryTestForImage(Message message, boolean isSizeLimit, int height, int width, String... formats) {
		assertFalse(message.getAttachments().isEmpty());
		Message.Attachment attachment = message.getAttachments().get(0);
		assertTrue(attachment.isImage());
		if (isSizeLimit) {
			assertTrue(height >= attachment.getHeight());
			assertTrue(width >= attachment.getWidth());
		} else {
			assertEquals(height, attachment.getHeight());
			assertEquals(width, attachment.getWidth());

		}
		assertEquals(1, Stream.of(formats).filter(x -> attachment.getUrl().endsWith(x)).count());
		//Maximun file size allowed
		assertTrue(attachment.getSize() <= 8388608);

	}

	public static void testImage(String command, int height, int width, int timeout, String... formats) {
		testImage(command, false, height, width, timeout, formats);
	}

	public static void testImage(String command, boolean isSizeLimit, int height, int width, String... formats) {
		testImage(command, isSizeLimit, height, width, 45, formats);
	}

	public static void testImageWithPreWarning(String command, String warningMessage, int height, int width, String... formats) {
		testImageWithPreWarning(command, warningMessage, false, height, width, 45, formats);
	}

	public static void testImageWithPreWarning(String command, String warningMessage, boolean isSizeLimit, int height, int width, int timeout, String... formats) {
		long id = channelWorker.sendMessage(command).complete().getIdLong();
		await().atMost(timeout, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 2;
		});
		Message warning = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(1);
		assertEquals(warning.getContentStripped(), warningMessage);
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		batteryTestForImage(message, isSizeLimit, height, width, formats);

	}

	public static void testImageWithPreWarning(String command, String warningMessage, int height, int width, int timeout, String... formats) {
		testImageWithPreWarning(command, warningMessage, false, height, width, timeout, formats);
	}

	public static void testImageWithPreWarning(String command, String warningMessage, boolean isSizeLimit, int height, int width, String... formats) {
		testImageWithPreWarning(command, warningMessage, isSizeLimit, height, width, 45, formats);
	}

}
