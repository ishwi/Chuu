package core.imagerenderer.util;

import java.awt.*;

public enum PieTheme {
    PINK_THEME, DARK_THEME, BLUE_THEME;


    public Color getBackgroundColor() {
        switch (this) {
            case PINK_THEME:
                return Color.decode("#f6def6");
            case DARK_THEME:
                return Color.decode("#2c2f33");
            case BLUE_THEME:
                return Color.decode("#85c1e5");
            default:
                throw new IllegalStateException();
        }
    }

    public Color getBesselColour() {

        switch (this) {
            case PINK_THEME:
                return Color.decode("#ffa5b0");
            case DARK_THEME:
                return Color.decode("#23272a");
            case BLUE_THEME:
                return Color.decode("#254e7b");
            default:
                throw new IllegalStateException();
        }
    }

    public Color getTitleColour() {

        switch (this) {
            case PINK_THEME:
                return Color.decode("#ffa5b0");
            case DARK_THEME:
                return Color.decode("#23272a");
            case BLUE_THEME:
                return Color.decode("#5584b1");
            default:
                throw new IllegalStateException();
        }

    }

}
