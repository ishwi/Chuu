package test.commands;

import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Test;
import test.commands.utils.TestResources;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

public class WhoKnowsAlbumCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!wka";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.artistAlbumParser(COMMAND_ALIAS);
	}

	@Test
	public void imageTest() {

		ImageUtils.testImage(COMMAND_ALIAS + " RED VELVET - Perfect Velvet \\- The 2nd Album", 500, 800, ".png");
	}


	@Test
	public void NoOneKnows() {
		long id = TestResources.channelWorker.sendMessage(COMMAND_ALIAS + " NOT A KNOWN - ARTIST BTW ").complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = TestResources.channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = TestResources.channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		assertEquals("No one knows NOT A KNOWN - ARTIST BTW", message.getContentStripped());
	}


	@Test
	public void scoreErrorMessage() {
		NullReturnParsersTest.scoreOnAlbumError(COMMAND_ALIAS);
	}

	@Test
	public void imageSongTest() {
		ImageUtils.testImage("!wkt " , 500, 800, ".png");
	}


	@Test
	public void NoOneKnowsSongTest() {
		long id = TestResources.channelWorker.sendMessage("!wkt" + " NOT A KNOWN - SONG BTW ").complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = TestResources.channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = TestResources.channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		assertEquals("No one knows NOT A KNOWN - SONG BTW", message.getContentStripped());
	}


}
