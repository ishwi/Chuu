package test.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.junit.jupiter.api.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.ImageUtils;
import test.commands.utils.OneLineUtils;
import test.commands.utils.TestResources;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
        long id = TestResources.channelWorker.sendMessage(COMMAND_ALIAS + " NOT A KNOWN - ARTIST BTW ").complete()
                .getIdLong();
        await().atMost(45, TimeUnit.SECONDS).until(() ->
        {
            MessageHistory complete = TestResources.channelWorker.getHistoryAfter(id, 20).complete();
            return complete.getRetrievedHistory().size() == 1;
        });
        Message message = TestResources.channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
        assertThat(message.getContentStripped()).isEqualTo("No one knows NOT A KNOWN - ARTIST BTW");
    }


    @Test
    public void scoreErrorMessage() {
        NullReturnParsersTest.scoreOnAlbumError(COMMAND_ALIAS);
    }

    @Test
    public void imageSongTest() {

        ImageUtils.testImage("!wkt blackpink - boombayah ", 500, 800, ".png");
    }


    @Test
    public void NoOneKnowsSongTest() {

        OneLineUtils.testCommands("!wkt  NOT A KNOWN - SONG BTW", Pattern
                .compile("No one knows NOT A KNOWN - SONG BTW"), null);

    }

    @Test
    public void NowPlayingTest() {

        long id = TestResources.channelWorker.sendMessage("!wkt").complete()
                .getIdLong();
        await().atMost(45, TimeUnit.SECONDS).until(() ->
        {
            MessageHistory complete = TestResources.channelWorker.getHistoryAfter(id, 20).complete();
            return complete.getRetrievedHistory().size() == 1;
        });
        Message message = TestResources.channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
        if (message.getAttachments().isEmpty()) {
            Pattern compile = Pattern.compile("No one knows (.*)");
            assertThat(compile.matcher(message.getContentStripped()).matches()).isTrue();
        } else {
            Message.Attachment attachment = message.getAttachments().get(0);
            assertThat(attachment.getHeight()).isEqualTo(500);
            assertThat(attachment.getWidth()).isEqualTo(800);
            assertThat(attachment.getFileName().endsWith(".png")).isTrue();
        }
    }


}
