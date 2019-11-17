package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.ImageUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static main.commands.utils.TestResources.channelWorker;
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
		long id = channelWorker.sendMessage(COMMAND_ALIAS + " NOT A KNOWN - ARTIST BTW ").complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		assertEquals("No one knows NOT A KNOWN - ARTIST BTW", message.getContentStripped());
	}


	@Test
	public void scoreErrorMessage() {
		NullReturnParsersTest.scoreOnAlbumError(COMMAND_ALIAS);
	}

	@Test
	public void imageSongTest() {

		ImageUtils.testImage("!wkt" + " billie eilish - bad guy", 500, 800, ".png");
	}


	@Test
	public void NoOneKnowsSongTest() {
		long id = channelWorker.sendMessage("!wkt" + " NOT A KNOWN - SONG BTW ").complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		assertEquals("No one knows NOT A KNOWN - SONG BTW", message.getContentStripped());
	}
}
