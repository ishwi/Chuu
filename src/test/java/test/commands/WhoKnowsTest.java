package test.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.EmbedUtils;
import test.commands.utils.ImageUtils;
import test.commands.utils.TestResources;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
        //"|week||(?:Album )?Crowns leaderboard||"

        String commonArtist = TestResources.commonArtist;
        String artistUrl = TestResources.dao.getArtistUrl(commonArtist);
        EmbedUtils
                .testLeaderboardEmbed(COMMAND_ALIAS + " " + TestResources.commonArtist + " --list", EmbedUtils.descriptionArtistRegex, "Who knows (.*?)(?= in ${header}\\?) in ${header}\\?", true, false, artistUrl,
                        Pattern.compile("No one knows (.*)"));
    }

	@Test
	public void NoOneKnows() {
		long id = TestResources.channelWorker.sendMessage(COMMAND_ALIAS + " NOT A KNOWN ARTIST BTW ").complete().getIdLong();
		await().atMost(45, TimeUnit.SECONDS).until(() ->
		{
			MessageHistory complete = TestResources.channelWorker.getHistoryAfter(id, 20).complete();
			return complete.getRetrievedHistory().size() == 1;
		});
		Message message = TestResources.channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
		assertEquals("No one knows NOT A KNOWN ARTIST BTW", message.getContentStripped());
	}

}
