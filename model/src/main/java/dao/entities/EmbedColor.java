package dao.entities;

import org.beryx.awt.color.ColorFactory;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public record EmbedColor(dao.entities.EmbedColor.EmbedColorType type, List<String> colorList) {

    private static final Pattern hex = Pattern.compile("[0-9a-fA-F]+");

    public static EmbedColor defaultColor() {
        return new EmbedColor(EmbedColorType.RANDOM, Collections.emptyList());
    }

    public static @Nullable
    EmbedColor fromString(String string) {
        if (string == null) {
            return null;
        }
        if (string.equalsIgnoreCase("random")) {
            return new EmbedColor(EmbedColorType.RANDOM, Collections.emptyList());
        }
        if (string.equalsIgnoreCase("role")) {
            return new EmbedColor(EmbedColorType.ROLE, Collections.emptyList());
        } else {
            String[] words = string.split("\s+");
            Set<String> colorList = new LinkedHashSet<>();
            for (String s : words) {
                try {

                    if (hex.matcher(s).matches()) {
                        s = "#" + s;
                    }
                    Color color = ColorFactory.valueOf(s);
                    colorList.add(s);
                } catch (IllegalArgumentException ignored) {
                }
            }
            return new EmbedColor(EmbedColorType.COLOURS, new ArrayList<>(colorList));
        }
    }

    @Override
    public String toString() {
        return switch (type) {
            case RANDOM -> null;
            case ROLE -> "ROLE";
            case COLOURS -> String.join(" ", colorList);
        };

    }

    public String toDisplayString() {
        return switch (type) {
            case RANDOM -> "Random Colour";
            case ROLE -> "Role Colour";
            case COLOURS -> "one of ➜ " + String.join(" | ", colorList);
        };

    }

    public Color[] mapList() {
        return colorList.stream().map(s -> {
            if (hex.matcher(s).matches()) {
                s = "#" + s;
            }

            return ColorFactory.valueOf(s);
        }).toArray(Color[]::new);

    }

    public boolean isValid() {
        return switch (type) {
            case RANDOM, ROLE -> true;
            case COLOURS -> String.join(" ", colorList).length() < 200;
        };
    }

    public enum EmbedColorType {
        RANDOM("Uses a random colour"), ROLE("Uses the color of your role in a server"), COLOURS("Uses user defined colours");

        private final String description;

        EmbedColorType(String s) {
            description = s;
        }

        public String getDescription() {
            return description;
        }
    }
}
