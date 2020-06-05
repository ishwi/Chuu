package dao.entities;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum GayType {
    LGTBQ(6), BI(5), TRANS(5);

    private final int columns;
    private static final Map<GayType, java.util.List<Color>> palettes;

    static {
        palettes = Arrays.stream(GayType.values()).collect(Collectors.toMap(x -> x,
                x -> {
                    switch (x) {
                        case LGTBQ:
                            return java.util.List.of(
                                    Color.decode("#FF0018"),
                                    Color.decode("#FFA52C"),
                                    Color.decode("#FFFF41"),
                                    Color.decode("#008018"),
                                    Color.decode("#0000F9"),
                                    Color.decode("#86007D"));
                        case BI:
                            return java.util.List.of(Color.decode("#D60270"),
                                    Color.decode("#D60270"),
                                    Color.decode("#9B4F96"),
                                    Color.decode("#0038A8"),
                                    Color.decode("#0038A8"));
                        case TRANS:
                            return java.util.List.of(Color.decode("#55CDFC"),
                                    Color.decode("#F7A8B8"),
                                    Color.decode("#FFFFFF"),
                                    Color.decode("#F7A8B8"),
                                    Color.decode("#55CDFC"));
                    }
                    throw new UnsupportedOperationException();
                }));
    }

    GayType(int i) {
        columns = i;
    }

    public List<Color> getPalettes() {
        return palettes.get(this);
    }


    public int getColumns() {
        return columns;
    }
}
