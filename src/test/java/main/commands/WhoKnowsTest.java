package main.commands;

import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.EmbedUtils;
import main.commands.utils.ImageUtils;
import main.commands.utils.TestResources;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static main.commands.utils.TestResources.channelWorker;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

public class WhoKnowsTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!whoknows";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.artistParser(COMMAND_ALIAS);
	}

	@Test
	public void imageTest() {
		ImageUtils.testImage(COMMAND_ALIAS + " " + TestResources.commonArtist, 500, 800, ".png");
	}

	@Test
	public void EmbedTest() {
		//"|week||(?:Album )?Crowns leadearboard||"

		String commonArtist = TestResources.commonArtist;
		String artistUrl = TestResources.dao.getArtistUrl(commonArtist);
		EmbedUtils
				.testLeaderboardEmbed(COMMAND_ALIAS + " " + TestResources.commonArtist + " --list", EmbedUtils.descriptionArtistRegex, "Who knows (.*?)(?= in ${header}\\?) in ${header}\\?", true, false, artistUrl,
						Pattern.compile("No one knows (.*)"));
	}

	@Test
	public void NoOneKnows() {
		long id = channelWorker.sendMessage(COMMAND_ALIAS + " NOT A KNOWN ARTIST BTW ").complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		assertEquals("No one knows NOT A KNOWN ARTIST BTW", message.getContentStripped());
	}

}
