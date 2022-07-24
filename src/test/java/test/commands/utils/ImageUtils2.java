package test.commands.utils;

import test.commands.parsers.EventEmitter;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageUtils2 {

    public static void testImage(EventEmitter.SendImage event, int height, int width, String... formats) {
        testImage(event, false, height, width, 45, formats);
    }

    public static void testImage(EventEmitter.SendImage event, boolean isSizeLimit, int height, int width, int timeout, String... formats) {
        batteryTestForImage(event, isSizeLimit, height, width, formats);
    }

    public static void batteryTestForImage(EventEmitter.SendImage event, boolean isSizeLimit, int height, int width, String... formats) {
        try (InputStream io = event.io()) {

            byte[] bytes = io.readAllBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            var read = ImageIO.read(bais);
            assertThat(read != null).isTrue();
            assertThat(read.getHeight()).isEqualTo(height);
            assertThat(read.getWidth()).isEqualTo(width);
            //Maximun file size allowed
            assertThat(bytes.length).isLessThan(8388608);

            String filename = event.filename();
            assertThat(filename).containsAnyOf(formats);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static void testImage(EventEmitter.SendImage event, int height, int width, int timeout, String... formats) {
        testImage(event, false, height, width, timeout, formats);
    }

    public static void testImage(EventEmitter.SendImage event, boolean isSizeLimit, int height, int width, String... formats) {
        testImage(event, isSizeLimit, height, width, 45, formats);
    }


}
