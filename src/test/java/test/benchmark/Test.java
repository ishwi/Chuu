package test.benchmark;


import core.imagerenderer.stealing.blur.GaussianFilter;
import org.openjdk.jmh.annotations.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

@State(Scope.Benchmark)
public class Test {

    @Param({"10"})
    public int iterations;

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public void measureName() {

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void init(ExecutionPlan plan) {
        for (int i = plan.iterations; i > 0; i--) {
            new GaussianFilter(90).filter(plan.getImage(), null);
        }
    }
}
