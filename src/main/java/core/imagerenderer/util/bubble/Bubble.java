package core.imagerenderer.util.bubble;

import java.awt.*;

public final class Bubble {
    public double x;
    public double y;
    public double radius;
    public String name;
    public Color color;
    public Bubble insertNext;
    public Bubble next;
    public Bubble prev;

    public Bubble(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }


}
