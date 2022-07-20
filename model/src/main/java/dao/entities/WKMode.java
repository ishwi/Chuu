package dao.entities;


import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

public enum WKMode {
    NORMAL(0),
    OWN_RANK(1),
    BETA(2),
    UNKNOWN(-1);

    private final long raw;


    WKMode(int offset) {
        this.raw = 1L << offset;
    }


    /**
     * This is effectively the opposite of {@link #getNPMode(long)} (long)}, this takes 1 or more {@link WKMode Modes}
     * and returns the raw offset {@code long} representation of the mode.
     *
     * @param modes The array of modes of which to form into the raw long representation.
     * @return Unsigned long representing the provided mode.
     */
    public static long getRaw(@Nonnull WKMode... modes) {
        long raw = 0;
        for (WKMode perm : modes) {
            if (perm != UNKNOWN)
                raw |= perm.raw;
        }
        return raw;
    }

    /**
     * A set of all {@link WKMode Modes} that are specified by this raw long representation of
     * modes.
     *
     * @param modeRaw The raw {@code long} representation of modes.
     * @return Possibly-empty EnumSet of {@link  WKMode modes}.
     */
    @Nonnull
    public static EnumSet<WKMode> getNPMode(long modeRaw) {
        if (modeRaw == 0L)
            return EnumSet.noneOf(WKMode.class);
        if (modeRaw == -1L)
            return EnumSet.of(WKMode.UNKNOWN);
        EnumSet<WKMode> modes = EnumSet.noneOf(WKMode.class);
        for (WKMode mode : WKMode.values()) {
            if (mode != UNKNOWN && (modeRaw & mode.raw) == mode.raw)
                modes.add(mode);
        }
        return modes;
    }

    public static String getListedName(Collection<WKMode> modes) {
        return modes.stream().filter(x -> !x.equals(UNKNOWN)).map(WKMode::toString).collect(Collectors.joining(" | "));
    }

    public String toString() {
        return WordUtils.capitalizeFully(super.toString(), '-', '_').replaceAll("_", "-");
    }

    public String getHelpMessage() {
        return switch (this) {
            case NORMAL -> "The normal np behaviour.";
            case OWN_RANK -> "Include your ranking even if you are not in the frontpage";
            case BETA -> "Experimental visualization for the wk";
            case UNKNOWN -> throw new IllegalStateException("Unexpected value: " + this);
        };

    }
}
