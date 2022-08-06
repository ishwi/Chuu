package core.imagerenderer.util.bubble;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.Math.*;

public record BubblePacker(List<StringFrequency> frequencies, Supplier<Color> colorSupplier) {


    private static void splice(Bubble a, Bubble b) {
        a.next = b;
        b.prev = a;
    }

    private static void place(Bubble a, Bubble b, Bubble c) {
        double da = b.radius + c.radius;
        double db = a.radius + c.radius;
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dc = sqrt(dx * dx + dy * dy);
        if (dc > 0.0) {
            double cos = (db * db + dc * dc - da * da) / (2 * db * dc);
            double theta = acos(cos);
            double x = cos * db;
            double h = sin(theta) * db;
            dx /= dc;
            dy /= dc;
            c.x = a.x + x * dx + h * dy;
            c.y = a.y + x * dy - h * dx;
        } else {
            c.x = a.x + db;
            c.y = a.y;
        }
    }

    private static double distance(Bubble a) {
        return sqrt(a.x * a.x + a.y * a.y);
    }

    private static boolean intersects(Bubble a, Bubble b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dr = a.radius + b.radius;
        // overlap is bigger than epsilon
        return dr * dr - 1e-6 > dx * dx + dy * dy;
    }

    private static void insert(Bubble a, Bubble b) {
        Bubble c = a.next;
        a.next = b;
        b.prev = a;
        b.next = c;
        if (c != null) c.prev = b;
    }

    private static void bound(Bubble n, Bubble topright, Bubble bottomleft) {
        bottomleft.x = Math.min(n.x - n.radius, bottomleft.x);
        bottomleft.y = Math.min(n.y - n.radius, bottomleft.y);
        topright.x = Math.max(n.x + n.radius, topright.x);
        topright.y = Math.max(n.y + n.radius, topright.y);
    }

    private static Bubble placeCircles(Bubble firstnode, Bubble bb_topright, Bubble bb_bottomleft) {
        Bubble a = firstnode;
        a.x = -1 * a.radius;
        if ((a.insertNext == null)) {
            return a;
        }
        Bubble b = a.insertNext;
        b.x = b.radius;
        b.y = 0;
        bound(b, bb_topright, bb_bottomleft);

        if (b.insertNext == null) {
            return a;
        }
        Bubble c = b.insertNext;
        place(a, b, c);
        bound(c, bb_topright, bb_bottomleft);

        if (c.insertNext == null) {
            return a;
        }

        // make initial chain of a <.b <.c
        a.next = c;
        a.prev = b;
        b.next = a;
        b.prev = c;
        c.next = b;
        c.prev = a;
        b = c;


        /* add remaining nodes */
        boolean skip = false;
        c = c.insertNext;
        while (c != null) {
            if (!skip) {
                Bubble n = a;
                Bubble nearestNode = n;
                double nearestDist = Double.MAX_VALUE;
                do {
                    double dist_n = distance(n);
                    if (dist_n < nearestDist) {
                        nearestDist = dist_n;
                        nearestNode = n;
                    }
                    n = n.next;
                } while (n != a);

                a = nearestNode;
                b = nearestNode.next;
                skip = false;
            }
            /* a corresponds to C_m, and b corresponds to C_n in the paper */
            place(a, b, c);
            /* for debugging: initial placement of c that may ovelap */
            //a.append(String.format("<circle cx=\"%.5f\" cy=\"%.5f\" r=\"%.5f\" stroke=\"black\" stroke-width=\"3\" fill=\"red\" />\n",c.x, c.y, c.radius);
            int isect = 0;
            Bubble j = b.next;
            Bubble k = a.prev;
            double sj = b.radius;
            double sk = a.radius;
            //j = b.next, k = a.previous, sj = b._.r, sk = a._.r;
            do {
                if (sj <= sk) {
                    if (intersects(j, c)) {
                        splice(a, j);
                        b = j;
                        skip = true;
                        isect = 1;
                        break;
                    }
                    sj += j.radius;
                    j = j.next;
                } else {
                    if (intersects(k, c)) {
                        splice(k, b);
                        a = k;
                        skip = true;
                        isect = 1;
                        break;
                    }
                    sk += k.radius;
                    k = k.prev;
                }
            } while (j != k.next);

            if (isect == 0) {
                /* Update node chain. */
                insert(a, c);
                b = c;
                bound(c, bb_topright, bb_bottomleft);
                skip = true;
                c = c.insertNext;
            }
        }

        return a;

    }

    static String printSVG(Bubble first, Bubble a_, Bubble bb_topright, Bubble bb_bottomleft) {
        double spacing = Math.max(bb_topright.y + Math.abs(bb_bottomleft.y), bb_topright.x + Math.abs(bb_bottomleft.x)) / 400.0;
        double height = (bb_topright.y + Math.abs(bb_bottomleft.y)) + 2 * spacing;
        double width = (bb_topright.x + Math.abs(bb_bottomleft.x)) + 2 * spacing;
        int viewport_width = 640;
        int viewport_height = 480;
        // scaling of stroke-width with the size of the image
        double stroke_width = viewport_width / 400. * (width / viewport_width);
        StringBuilder a = new StringBuilder()
                .append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"%d\" width=\"%d\" viewBox=\"0 0 %.5f %.5f\" preserveAspectRatio=\"xMidYMid meet\">\n", viewport_height, viewport_width, width, height));
        a.append("<defs>\n");
        a.append("<style type=\"text/css\"><![CDATA[\n");
        a.append(String.format("  .circle_c { fill:#eee; stroke: #444; stroke-width: %.5f }\n", stroke_width));
        // optionally:  a.append(String.format("  .circle_c:hover { stroke: #444; stroke-width: %.5f }\n",2*stroke_width);
        a.append("]]></style>\n");
        a.append("</defs>\n");
        a.append(String.format("<g transform=\"translate(%.5f,%.5f)\">\n", (width) / 2.0, (height) / 2.0));

        double offset_x = (bb_bottomleft.x + bb_topright.x) / 2.0;
        double offset_y = (bb_bottomleft.y + bb_topright.y) / 2.0;

        Bubble n = first;
        while (n != null) {
            n.x -= offset_x;
            n.y -= offset_y;
            a.append(String.format("<g><title>%s (num=%d)</title><circle cx=\"%.5f\" cy=\"%.5f\" r=\"%.5f\" style=\"fill:%s\" class=\"circle_c\"/></g>\n", "", 1, n.x, n.y, n.radius, ""));

            //for debug, node number
            //a.append(String.format("<text x=\"%.5f\" y=\"%.5f\" stroke=\"black\" stroke-width=\"1\">%d</text>\n",n.x, n.y, n.num);
            //for debug, lines follow insertion order
            //if(next) a.append(String.format("<line x1=\"%.5f\" y1=\"%.5f\" x2=\"%.5f\" y2=\"%.5f\" style=\"stroke:black;stroke-width:2;\" />",n.x,n.y,next.x,next.y);
            n = n.insertNext;
        }
        /* print last node chain */

        a.append("</g>\n");
        a.append("</svg>\n");
        return a.toString();
    }

    public String toSVG() {
        Bubble lastInsertedNode = null;

        Bubble bb_bottomleft = new Bubble(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        Bubble bb_topright = new Bubble(Integer.MIN_VALUE, Integer.MIN_VALUE, 0);


        Bubble firstNode = toNode(frequencies);
        Bubble a = placeCircles(firstNode, bb_topright, bb_bottomleft);
        return printSVG(firstNode, a, bb_topright, bb_bottomleft);
    }

    public BufferedImage toImage() {
        return null;
    }

    private Bubble map(StringFrequency circle, double max) {
        Bubble bubble = new Bubble(-1, -1, (circle.freq() / max) * 150);
        bubble.color = colorSupplier.get();
        return bubble;
    }

    private Bubble toNode(List<StringFrequency> node) {
        if (node.isEmpty()) {
            return null;
        }
        long max = node.stream().mapToLong(StringFrequency::freq).max().orElse(1);
        Bubble firstBubble = null;
        Bubble lastinsertednode = null;
        for (StringFrequency circle : node) {
            Bubble n = map(circle, max);
            if (firstBubble == null) {
                firstBubble = n;
            } else {
                lastinsertednode.insertNext = n;
            }
            lastinsertednode = n;
        }
        return firstBubble;
    }
}
