package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static main.commands.TestResources.channelWorker;
import static org.awaitility.Awaitility.await;

public class ChartCommandTest extends CommandTest {

	@Override
	public String giveCommandName() {
		return "!chart";
	}

	@Test
	public void nullParserReturned() {
		NullReturnParsersTest.chartParser(COMMAND_ALIAS);
	}

	@Test
	public void ChartNormalTest() {
		long id = channelWorker.sendMessage(COMMAND_ALIAS + " a 1x1").complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		Assert.assertFalse(message.getAttachments().isEmpty());
		Message.Attachment attachment = message.getAttachments().get(0);
		Assert.assertTrue(attachment.isImage());
		Assert.assertEquals(300, attachment.getHeight());
		Assert.assertEquals(300, attachment.getWidth());
		Assert.assertTrue(attachment.getUrl().endsWith(".png"));

		//Maximun file size allowed
		Assert.assertTrue(attachment.getSize() <= 8388608);


	}

	@Test
	public void ChartBigTest() {
		long id = channelWorker.sendMessage("!chart a 10x6").complete().getIdLong();
		await().atMost(60, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		Assert.assertFalse(message.getAttachments().isEmpty());
		Message.Attachment attachment = message.getAttachments().get(0);
		Assert.assertTrue(attachment.isImage());
		Assert.assertEquals(900, attachment.getHeight());
		Assert.assertEquals(1500, attachment.getWidth());
		Assert.assertTrue(attachment.getUrl().endsWith(".jpg"));
		//Maximun file size allowed
		Assert.assertTrue(attachment.getSize() <= 8388608);
	}

	@Test
	public void ChartOptionalsTest() {
		long id = channelWorker.sendMessage("!chart a 1x1 --notitles --plays").complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		Assert.assertFalse(message.getAttachments().isEmpty());
		Message.Attachment attachment = message.getAttachments().get(0);
		Assert.assertTrue(attachment.isImage());
		Assert.assertEquals(300, attachment.getHeight());
		Assert.assertEquals(300, attachment.getWidth());
		Assert.assertTrue(attachment.getUrl().endsWith(".png"));
		//Maximun file size allowed
		Assert.assertTrue(attachment.getSize() <= 8388608);
	}


}
