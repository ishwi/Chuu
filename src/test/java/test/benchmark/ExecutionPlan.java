package test.benchmark;

import core.imagerenderer.GraphicUtils;
import core.imagerenderer.stealing.blur.BoxBlurFilter;
import org.openjdk.jmh.annotations.*;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

@State(Scope.Benchmark)
public class ExecutionPlan {

    @Param({"10"})
    public int iterations;


    private BufferedImage image;
    private BufferedImage background;
    private Queue<BufferedImage> images = new LinkedList<>();

    public static void main(String[] args) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setUp();
        for (int i = executionPlan.iterations; i > 0; i--) {
            new BoxBlurFilter().filter(executionPlan.getImage(), executionPlan.getImages().poll());
        }
    }

    @Setup(Level.Invocation)
    public void setUp() {
        image = GraphicUtils.getImage("https://lastfm.freetls.fastly.net/i/u/770x0/a13785107bbfa3f91b85eb4342961f03.png");
        background = new BufferedImage(900, 600, BufferedImage.TYPE_4BYTE_ABGR);
        images = new LinkedList<>();
    }

    public Queue<BufferedImage> getImages() {
        return images;
    }

    public void setImages(Queue<BufferedImage> images) {
        this.images = images;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }


    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getBackground() {
        return background;
    }

    public void setBackground(BufferedImage background) {
        this.background = background;
    }
}
