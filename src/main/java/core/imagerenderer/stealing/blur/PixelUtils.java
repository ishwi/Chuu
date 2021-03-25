package core.imagerenderer.stealing.blur;
/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */


/**
 * Some more useful math functions for image processing.
 * These are becoming obsolete as we move to Java2D. Use MiscComposite instead.
 */
class PixelUtils {

    /**
     * Clamp a value to the range 0..255
     */
    static int clamp(int c) {
        if (c < 0)
            return 0;
        return Math.min(c, 255);

    }


    /**
     * Clamp a value to an interval.
     *
     * @param a the lower clamp threshold
     * @param b the upper clamp threshold
     * @param x the input parameter
     * @return the clamped value
     */
    public static float clamp(float x, float a, float b) {
        return (x < a) ? a : Math.min(x, b);
    }

    /**
     * Clamp a value to an interval.
     *
     * @param a the lower clamp threshold
     * @param b the upper clamp threshold
     * @param x the input parameter
     * @return the clamped value
     */
    public static int clamp(int x, int a, int b) {
        return (x < a) ? a : Math.min(x, b);
    }


}
