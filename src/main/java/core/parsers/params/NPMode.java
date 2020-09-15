package core.parsers.params;


import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum NPMode {
    NORMAL(0),
    PREVIOUS(1),
    TAGS(2),
    CROWN(3),
    LFM_LISTENERS(4),
    LFM_SCROBBLES(5),
    ALBUM_RYM(6),
    ARTIST_RANK(7),
    SERVER_ALBUM_RYM(8),
    BOT_ALBUM_RYM(9),
    ALBUM_RANK(10),
    ALBUM_CROWN(11),
    GLOBAL_CROWN(12),
    GLOBAL_RANK(13),
    GLOBAL_ALBUM_CROWN(14),
    GLOBAL_ALBUM_RANK(15),
    SERVER_LISTENERS(16),
    SERVER_SCROBBLES(17),
    BOT_LISTENERS(16),
    BOT_SCROBBLES(17),
    GENDER(18),
    COUNTRY(19),
    ARTIST_PIC(20),
    UNKNOWN(-1);

    private final int offset;
    private final long raw;


    NPMode(int offset) {
        this.offset = offset;
        this.raw = 1 << offset;
    }

    public int getOffset() {
        return offset;
    }

    public long getRaw() {
        return raw;
    }


    /**
     * Gets the first {@link NPMode Permission} relating to the provided offset.
     * <br>If there is no {@link NPMode Permssions} that matches the provided
     * offset, {@link NPMode#NORMAL NPMode.NORMAL} is returned.
     *
     * @param offset The offset to match a {@link NPMode NPMode} to.
     * @return {@link NPMode NPMode} relating to the provided offset.
     */
    @Nonnull
    public static NPMode getFromOffset(int offset) {
        for (NPMode perm : values()) {
            if (perm.offset == offset)
                return perm;
        }
        return UNKNOWN;
    }

    /**
     * This is effectively the opposite of {@link #getNPMode(long)} (long)}, this takes 1 or more {@link NPMode Modes}
     * and returns the raw offset {@code long} representation of the mode.
     *
     * @param modes The array of modes of which to form into the raw long representation.
     * @return Unsigned long representing the provided mode.
     */
    public static long getRaw(@Nonnull NPMode... modes) {
        long raw = 0;
        for (NPMode perm : modes) {
            if (perm != UNKNOWN)
                raw |= perm.raw;
        }
        return raw;
    }

    /**
     * A set of all {@link NPMode Modes} that are specified by this raw long representation of
     * modes.
     *
     * @param modeRaw The raw {@code long} representation of modes.
     * @return Possibly-empty EnumSet of {@link  NPMode modes}.
     */
    @Nonnull
    public static EnumSet<NPMode> getNPMode(long modeRaw) {
        if (modeRaw == 0)
            return EnumSet.noneOf(NPMode.class);
        EnumSet<NPMode> modes = EnumSet.noneOf(NPMode.class);
        for (NPMode mode : NPMode.values()) {
            if (mode != UNKNOWN && (modeRaw & mode.raw) == mode.raw)
                modes.add(mode);
        }
        return modes;
    }

    public String toString() {
        return WordUtils.capitalizeFully(super.toString(), '-', '_').replaceAll("_", "-");
    }

    public static String getListedName(Collection<NPMode> modes) {
        return modes.stream().filter(x -> !x.equals(UNKNOWN)).map(NPMode::toString).collect(Collectors.joining(" | "));
    }


}
