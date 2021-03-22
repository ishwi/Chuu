package core.imagerenderer.util.bubble;

import core.commands.utils.CommandUtil;
import core.imagerenderer.util.pie.MonochromeColourer;
import core.imagerenderer.util.pie.PieColourer;
import core.imagerenderer.util.pie.RandomPalette;
import core.parsers.params.CommandParameters;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public interface IBubbleable<T, Y extends CommandParameters> {

    List<StringFrequency> obtainFrequencies(T data, Y params);

    default String doBubble(Y params, T data) {

        List<StringFrequency> stringFrequencies = obtainFrequencies(data, params);
        PieColourer randomPalette;
        if (CommandUtil.rand.nextBoolean()) {
            randomPalette = new MonochromeColourer();
        } else {
            randomPalette = new RandomPalette();
        }
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Supplier<Color> colorSupplier = () -> randomPalette.setIndividualColour(atomicInteger.incrementAndGet(), stringFrequencies.size());

        return new BubblePacker(stringFrequencies, colorSupplier).toSVG();
    }
}
