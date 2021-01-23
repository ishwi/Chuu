package core.imagerenderer.util;

import java.awt.*;

public enum PieTheme {
    PINK_THEME, DARK_THEME, BLUE_THEME;


    public Color getBackgroundColor() {
        return switch (this) {
            case PINK_THEME -> Color.decode("#f6def6");
            case DARK_THEME -> Color.decode("#2c2f33");
            case BLUE_THEME -> Color.decode("#85c1e5");
        };
    }

    public Color getBesselColour() {

        return switch (this) {
            case PINK_THEME -> Color.decode("#ffa5b0");
            case DARK_THEME -> Color.decode("#23272a");
            case BLUE_THEME -> Color.decode("#254e7b");
        };
    }

    public Color getTitleColour() {

        return switch (this) {
            case PINK_THEME -> Color.decode("#ffa5b0");
            case DARK_THEME -> Color.decode("#23272a");
            case BLUE_THEME -> Color.decode("#5584b1");
        };

    }

}
