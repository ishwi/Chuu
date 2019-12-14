package core.imagerenderer.stealing;
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




}