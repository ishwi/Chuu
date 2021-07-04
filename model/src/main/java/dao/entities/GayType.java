package dao.entities;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum GayType {
    LGTBQ(6), BI(5), TRANS(5), NB(4), LESBIAN(5), ACE(4);

    private static final Map<GayType, java.util.List<Color>> palettes;

    static {
        palettes = Arrays.stream(GayType.values()).collect(Collectors.toMap(x -> x,
                x -> switch (x) {
                    case LGTBQ -> List.of(
                            Color.decode("#FF0018"),
                            Color.decode("#FFA52C"),
                            Color.decode("#FFFF41"),
                            Color.decode("#008018"),
                            Color.decode("#0000F9"),
                            Color.decode("#86007D"));
                    case BI -> List.of(Color.decode("#D60270"),
                            Color.decode("#D60270"),
                            Color.decode("#9B4F96"),
                            Color.decode("#0038A8"),
                            Color.decode("#0038A8"));
                    case TRANS -> List.of(Color.decode("#55CDFC"),
                            Color.decode("#F7A8B8"),
                            Color.decode("#FFFFFF"),
                            Color.decode("#F7A8B8"),
                            Color.decode("#55CDFC"));
                    case NB -> List.of(Color.decode("#FFF430"),
                            Color.decode("#FFFFFF"),
                            Color.decode("#9C59D1"),
                            Color.decode("#000000"));
                    case LESBIAN -> List.of(Color.decode("#D62900"),
                            Color.decode("#FF9B55"),
                            Color.decode("#FFFFFF"),
                            Color.decode("#D461A6"),
                            Color.decode("#A50062"));
                    case ACE -> List.of(Color.decode("#000000"),
                            Color.decode("#A4A4A4"),
                            Color.decode("#FFFFFF"),
                            Color.decode("#810081"));
                }));
    }

    private final int columns;

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
