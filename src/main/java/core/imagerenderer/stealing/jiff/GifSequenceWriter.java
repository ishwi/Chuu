//
//  GifSequenceWriter.java
//
//  Created by Elliot Kroo on 2009-04-25.
//
// This work is licensed under the Creative Commons Attribution 3.0 Unported
// License. To view a copy of this license, visit
// http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
// Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
// Modified some IDE warnings @ 2020-09-22 by Ishwara Coello Escobar.
package core.imagerenderer.stealing.jiff;

import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.util.List;

public class GifSequenceWriter {
    public static void saveGif(ImageOutputStream out, List<BufferedImage> images, int repeat, int delay) {
        GifEncoder gif = new GifEncoder();
        gif.setRepeat(repeat);
        gif.start(out);
        gif.setDelay(delay);
        for (BufferedImage image : images) {
            gif.addFrame(image);
        }
        gif.finish();
    }
}
