package dao.entities;

import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum ChartOptions {
    NONE(0),
    NOTITLES(1),
    PLAYS(2);
    private final long raw;


    ChartOptions(int offset) {
        this.raw = 1L << offset;
    }

    public static String getListedName(Collection<ChartOptions> modes) {
        return modes.stream().map(ChartOptions::toString).collect(Collectors.joining(" | "));
    }

    public static long getRaw(@Nonnull ChartOptions... opts) {
        long raw = 0;
        for (ChartOptions opt : opts) {
            if (opt != NONE)
                raw |= opt.raw;
        }
        return raw;
    }

    @Nonnull
    public static EnumSet<ChartOptions> getChartOptions(long modeRaw) {
        if (modeRaw == 0L || modeRaw == -1L)
            return ChartOptions.defaultMode();
        EnumSet<ChartOptions> modes = EnumSet.noneOf(ChartOptions.class);
        for (ChartOptions mode : ChartOptions.values()) {
            if (mode != NONE && (modeRaw & mode.raw) == mode.raw)
                modes.add(mode);
        }
        return modes;
    }

    public static EnumSet<ChartOptions> defaultMode() {
        return EnumSet.of(ChartOptions.NONE);
    }

    public String toString() {
        return WordUtils.capitalizeFully(super.toString(), '-', '_').replaceAll("_", "-");
    }
}
