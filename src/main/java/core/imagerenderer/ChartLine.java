package core.imagerenderer;


import java.util.Objects;

public class ChartLine {
    private final String line;
    private final Type type;

    public ChartLine(String line) {
        this(line, Type.NORMAL);
    }

    public ChartLine(String line, Type type) {
        this.line = line;
        this.type = type;
    }

    public String getLine() {
        return line;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChartLine)) return false;
        ChartLine chartLine = (ChartLine) o;
        return Objects.equals(getLine(), chartLine.getLine()) &&
               getType() == chartLine.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLine(), getType());
    }

    public enum Type {
        TITLE, NORMAL
    }
}
