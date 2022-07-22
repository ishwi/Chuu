package test.commands.utils;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.assertj.core.api.Assertions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(matcher.matches()).isFalse();
        if (function != null) {
            assertThat(function.test(matcher)).isFalse();
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
            messageAction = channelWorker.sendMessage(messageBuilder.setContent(command).build()).addFile(img, "cat.png");
        } catch (IOException e) {
            Assertions.fail("Shouldn't fail");
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
        assertThat(matcher.matches()).isFalse();
        if (function != null) {
            assertThat(function.test(matcher)).isFalse();
        }
    }
}
