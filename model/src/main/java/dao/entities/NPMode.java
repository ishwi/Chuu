package dao.entities;


import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
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
    GENDER(18),
    COUNTRY(19),
    ARTIST_PIC(20),
    RANDOM(21),
    ALBUM_PLAYS(22),
    ARTIST_PLAYS(23),
    SONG_PLAYS(24),
    HIGHEST_STREAK(25),
    HIGHEST_SERVER_STREAK(26),
    HIGHEST_BOT_STREAK(27),
    SPOTIFY_LINK(28),
    SONG_DURATION(29),
    BOT_LISTENERS(30),
    BOT_SCROBBLES(31),
    TRACK_RANK(32),
    TRACK_CROWN(33),
    GLOBAL_TRACK_CROWN(34),
    GLOBAL_TRACK_RANK(35),
    UNKNOWN(-1);

    private final int offset;
    private final long raw;


    NPMode(int offset) {
        this.offset = offset;
        this.raw = 1L << offset;
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
        if (modeRaw == 0L)
            return EnumSet.noneOf(NPMode.class);
        if (modeRaw == -1L)
            return EnumSet.of(NPMode.UNKNOWN);
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


    public String getHelpMessage() {
        String returnal;
        switch (this) {
            case NORMAL:
                returnal = "The normal np behaviour.";
                break;
            case PREVIOUS:
                returnal = "Will include an extra field showing your previous song.";
                break;
            case TAGS:
                returnal = "Give you some genre tags for your song/album/artist.";
                break;
            case CROWN:
                returnal = "Tells you who holds the crown in this server.";
                break;
            case LFM_LISTENERS:
                returnal = "Number of listeners of this artist in last.fm.";
                break;
            case LFM_SCROBBLES:
                returnal = "Number of scrobbles of this artist in last.fm.";
                break;
            case ALBUM_RYM:
                returnal = "Your rating in RYM of the current album";
                break;
            case ARTIST_RANK:
                returnal = "Your position towards the crown of the artist in this server.";
                break;
            case SERVER_ALBUM_RYM:
                returnal = "Number of ratings and average of an album in this server.";
                break;
            case BOT_ALBUM_RYM:
                returnal = "Number of ratings and average of an album in the bot.";
                break;
            case ALBUM_RANK:
                returnal = "Your position towards the crown of the album in this server.";
                break;
            case ALBUM_CROWN:
                returnal = "Tells you who holds the album crown in this server.";
                break;
            case GLOBAL_CROWN:
                returnal = "Tells you who holds the crown in the bot.";
                break;
            case GLOBAL_RANK:
                returnal = "Your position towards the crown of the artist in the bot.";
                break;
            case GLOBAL_ALBUM_CROWN:
                returnal = "Tells you who holds the album crown in the bot.";
                break;
            case GLOBAL_ALBUM_RANK:
                returnal = "Your position towards the crown of the album in the bot.";
                break;
            case SERVER_LISTENERS:
                returnal = "Number of listeners of this artist in this server.";
                break;
            case SERVER_SCROBBLES:
                returnal = "Number of scrobbles of this artist in this server.";
                break;
            case BOT_LISTENERS:
                returnal = "Number of listeners of this artist in the bot.";
                break;
            case BOT_SCROBBLES:
                returnal = "Number of scrobbles of this artist in the bot.";
                break;
            case GENDER:
                returnal = "Gender of the artist.";
                break;
            case COUNTRY:
                returnal = "Country of the artist.";
                break;
            case ARTIST_PIC:
                returnal = "Includes a small pic of the artist in the footer.";
                break;
            case RANDOM:
                returnal = "Will randomly select at most 5 other configs each time you do the np command.";
                break;
            case ALBUM_PLAYS:
                returnal = "Your scrobble count in the album.";
                break;
            case ARTIST_PLAYS:
                returnal = "Your scrobble count in the artist.";
                break;
            case SONG_PLAYS:
                returnal = "Your scrobble count in the song.";
                break;
            case SONG_DURATION:
                returnal = "The song duration.";
                break;
            case HIGHEST_STREAK:
                returnal = "Your highest streak of this artist.";
                break;
            case HIGHEST_SERVER_STREAK:
                returnal = "The highest streak of this artist in this server.";
                break;
            case HIGHEST_BOT_STREAK:
                returnal = "The highest streak of this artist in the bot.";
                break;
            case SPOTIFY_LINK:
                returnal = "A link if available of the song on spotify.";
                break;
            case TRACK_RANK:
                returnal = "Your position towards the crown of the track in this server.";
                break;
            case TRACK_CROWN:
                returnal = "Tells you who holds the track crown in this server.";
                break;
            case GLOBAL_TRACK_CROWN:
                returnal = "Tells you who holds the track crown in the bot.";
                break;
            case GLOBAL_TRACK_RANK:
                returnal = "Your position towards the track crown of the artist in the bot.";
                break;
            case UNKNOWN:
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        return returnal;

    }
}
