package test.commands.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static test.commands.utils.TestResources.channelWorker;

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
        assertThat(message.getAttachments().isEmpty()).isFalse();
        Message.Attachment attachment = message.getAttachments().get(0);
        assertThat(attachment.isImage()).isTrue();
        if (isSizeLimit) {
            assertThat(height >= attachment.getHeight()).isTrue();
            assertThat(width >= attachment.getWidth()).isTrue();
        } else {
            assertThat(height).isEqualTo(attachment.getHeight());
            assertThat(width).isEqualTo(attachment.getWidth());
        }
        assertThat(Stream.of(formats).filter(x -> attachment.getUrl().endsWith(x))).hasSize(1);
        //Maximun file size allowed
        assertThat(attachment.getSize() <= 8388608).isTrue();

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
        assertThat(warning.getContentStripped()).isEqualTo(warningMessage);
        Message message = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
        batteryTestForImage(message, isSizeLimit, height, width, formats);

    }


    public static void testImageWithPreWarningDeletable(String command, String warningMessage, boolean isSizeLimit, int height, int width, int timeout, String... formats) {
        long id = channelWorker.sendMessage(command).complete().getIdLong();
        await().atMost(timeout, TimeUnit.SECONDS).until(() ->
        {
            MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
            return complete.getRetrievedHistory().size() == 1;
        });

        Message warning = channelWorker.getHistoryAfter(id, 20).complete().getRetrievedHistory().get(0);
        assertThat(warning.getContentStripped()).isEqualTo(warningMessage);
        await().atMost(timeout, TimeUnit.SECONDS).until(() ->
        {
            MessageHistory complete = channelWorker.getHistoryAfter(id, 20).complete();
            return complete.getRetrievedHistory().size() == 1 && !complete.getRetrievedHistory().get(0).getAttachments()
                    .isEmpty();
        });
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
