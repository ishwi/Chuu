package core.parsers.params;

import core.commands.Context;

import java.awt.*;

public class AddRoleParameters extends CommandParameters {
    private final String rest;
    private final Color color;
    private final int start;
    private final int end;

    public AddRoleParameters(Context e, String rest, Color color, int first, int second) {
        super(e);

        this.rest = rest;
        this.color = color;
        this.start = first;
        this.end = second;
    }

    public String getRest() {
        return rest;
    }

    public Color getColor() {
        return color;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
