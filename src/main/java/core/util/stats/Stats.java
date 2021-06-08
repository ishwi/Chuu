package core.util.stats;

import core.apis.last.ConcurrentLastFM;
import core.commands.utils.CommandUtil;
import core.parsers.utils.CustomTimeFrame;
import core.util.stats.consumers.ConsumerUtils;
import core.util.stats.generator.GeneratorUtils;
import core.util.stats.generator.StatsCalculationException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.UserInfo;
import org.apache.commons.text.WordUtils;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public enum Stats {

    JOINED(GeneratorUtils.uInfo((a) ->
    {
        if (a.timeFrameEnum().isAllTime()) {
            return "**Scrobbling since:** " + CommandUtil.getAmericanizedDate(
                    OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(a.userInfo().getUnixtimestamp()),
                            ZoneId.systemDefault()
                    ));
        }
        return "Showing overview:" + a.timeFrameEnum().getDisplayString();
    }), AllMode.LINE_BREAK, "start", "since", "j"),
    PLAYS(GeneratorUtils.uInfo(ConsumerUtils::plays), "p", "scrobble"),

    ARTISTS(GeneratorUtils.aL(ConsumerUtils::artist)),
    ALBUMS(GeneratorUtils.albL(ConsumerUtils::albums)),
    SONGS(GeneratorUtils.tL(ConsumerUtils::songs), AllMode.LINE_BREAK, "track"),

    AVERAGES(GeneratorUtils.all(ConsumerUtils::averages), AllMode.LINE_BREAK, "lpa", "tpa", "tpl", "avg"),
    SCROBBLE_AVERAGES(GeneratorUtils.all(ConsumerUtils::scrobbleAverages), AllMode.LINE_BREAK, "spl", "spt", "spa", "savg", "sa"),

    H_INDEX(GeneratorUtils.aL((a, b) -> "**H-Index**: " + ConsumerUtils.HIndex(a)), "hi", "hind"),
    PERCENTAGE(GeneratorUtils.aL((list, ctx) -> ConsumerUtils.percentage(list, ctx, ConsumerUtils.Entity.ARTIST)), "per", "spct"),
    ALBUM_PERCENTAGE(GeneratorUtils.albL((list, ctx) -> ConsumerUtils.percentage(list, ctx, ConsumerUtils.Entity.ALBUM)), AllMode.DONT, "slper", "salbpct", "salbper"),
    SONG_PERCENTAGE(GeneratorUtils.albL((list, ctx) -> ConsumerUtils.percentage(list, ctx, ConsumerUtils.Entity.TRACK)), AllMode.DONT, "ssper", "sspct", "stper", "stpct"),
    TOP(GeneratorUtils.aL(ConsumerUtils::top)),
    SUM_TOP(GeneratorUtils.aL(ConsumerUtils::sumtop), AllMode.LINE_BREAK, "sum"),

    BREAKDOWNS(GeneratorUtils.aL((artists, entity) -> ConsumerUtils.breakdowns(artists, ConsumerUtils.Entity.ARTIST, entity)), AllMode.LINE_BREAK, "bk", "break", "point"),
    BREAKDOWNS_ALBUMS(GeneratorUtils.albL((artists, entity) -> ConsumerUtils.breakdowns(artists, ConsumerUtils.Entity.ALBUM, entity)), EnumSet.of(AllMode.LINE_BREAK, AllMode.DONT), "abk", "abreak", "apoint"),
    BREAKDOWNS_SONGS(GeneratorUtils.tL((artists, entity) -> ConsumerUtils.breakdowns(artists, ConsumerUtils.Entity.TRACK, entity)), EnumSet.of(AllMode.LINE_BREAK, AllMode.DONT), "sbk", "sbreak", "spoint"),

    BREADTH_RATING(GeneratorUtils.all(ConsumerUtils::breadth), "breadth", "breath", "bread", "br"),


    ARTISTS_OVER(GeneratorUtils.aL((a, b) -> ConsumerUtils.range(a, b, ConsumerUtils.Operation.OVER, ConsumerUtils.Entity.ARTIST)), AllMode.DONT, "ao"),
    ARTISTS_EQUAL(GeneratorUtils.aL((a, b) -> ConsumerUtils.range(a, b, ConsumerUtils.Operation.EQUAL, ConsumerUtils.Entity.ARTIST)), AllMode.DONT, "ae"),
    ARTISTS_UNDER(GeneratorUtils.aL((a, b) -> ConsumerUtils.range(a, b, ConsumerUtils.Operation.UNDER, ConsumerUtils.Entity.ARTIST)), AllMode.DONT, "au"),

    ALBUMS_OVER(GeneratorUtils.albL((a, b) -> ConsumerUtils.range(a, b, ConsumerUtils.Operation.OVER, ConsumerUtils.Entity.ALBUM)), AllMode.DONT, "lo", "albo", "alo"),
    ALBUMS_EQUAL(GeneratorUtils.albL((a, b) -> ConsumerUtils.range(a, b, ConsumerUtils.Operation.EQUAL, ConsumerUtils.Entity.ALBUM)), AllMode.DONT, "le", "albe", "ale"),
    ALBUMS_UNDER(GeneratorUtils.albL((a, b) -> ConsumerUtils.range(a, b, ConsumerUtils.Operation.UNDER, ConsumerUtils.Entity.ALBUM)), AllMode.DONT, "lu", "albu", "alu"),

    SONGS_OVER(GeneratorUtils.tL((a, b) -> ConsumerUtils.range(a, b, ConsumerUtils.Operation.OVER, ConsumerUtils.Entity.TRACK)), AllMode.DONT, "to", "so"),
    SONGS_EQUAL(GeneratorUtils.tL((a, b) -> ConsumerUtils.range(a, b, ConsumerUtils.Operation.EQUAL, ConsumerUtils.Entity.TRACK)), AllMode.DONT, "te", "se"),
    SONGS_UNDER(GeneratorUtils.tL((a, b) -> ConsumerUtils.range(a, b, ConsumerUtils.Operation.UNDER, ConsumerUtils.Entity.TRACK)), AllMode.DONT, "tu", "su"),

    ARTIST_PERCENT(GeneratorUtils.npArtist((a, b) -> ConsumerUtils.concretePercentage(a, b, ConsumerUtils.Entity.ARTIST)), AllMode.DONT, "aper", "apct"),
    ALBUM_PERCENT(GeneratorUtils.npAlbum((a, b) -> ConsumerUtils.concretePercentage(a, b, ConsumerUtils.Entity.ALBUM)), AllMode.DONT, "lper", "albper", "alper", "lpct", "albpct", "alpct"),
    TRACK_PERCENT(GeneratorUtils.npSong((a, b) -> ConsumerUtils.concretePercentage(a, b, ConsumerUtils.Entity.TRACK)), AllMode.DONT, "sper", "tper", "spct", "tpct"),

    ARTIST_RANK(GeneratorUtils.npArtist((a, b) -> ConsumerUtils.concreteRank(a, b, ConsumerUtils.Entity.ARTIST)), AllMode.DONT, "rank", "arank"),
    ALBUM_RANK(GeneratorUtils.npAlbum((a, b) -> ConsumerUtils.concreteRank(a, b, ConsumerUtils.Entity.ALBUM)), AllMode.DONT, "albrank", "alrank", "lrank"),
    SONG_RANK(GeneratorUtils.npSong((a, b) -> ConsumerUtils.concreteRank(a, b, ConsumerUtils.Entity.TRACK)), AllMode.DONT, "trank", "srank"),
    ALL;


    final boolean acceptsTimeframe;
    final Cache<?> cache;
    private final EnumSet<AllMode> mode;
    private final Set<String> aliases;

    Stats() {
        this.acceptsTimeframe = false;
        cache = null;
        mode = EnumSet.of(AllMode.NORMAL);
        this.aliases = Collections.emptySet();
    }


    <T> Stats(@NotNull Cache<T> cache) {
        this(cache, AllMode.NORMAL, new String[]{});
    }

    <T> Stats(@NotNull Cache<T> cache, String... aliases) {
        this(cache, AllMode.NORMAL, aliases);
    }


    <T> Stats(Cache<T> cache, AllMode mode) {
        this(cache, mode, new String[]{});
    }

    <T> Stats(Cache<T> cache, AllMode mode, String... aliases) {
        this(cache, EnumSet.of(mode), aliases);
    }

    <T> Stats(Cache<T> cache, EnumSet<AllMode> b, String... aliases) {
        this.mode = b;
        this.cache = cache;
        this.aliases = Set.of(aliases);
        this.acceptsTimeframe = false;
    }

    public static StatsResult process(LastFMData lastFMData,
                                      ChuuService db,
                                      ConcurrentLastFM lastFM,
                                      EnumSet<Stats> enumSet,
                                      UserInfo userInfo, int playsOnPeriod, int timestamp, CustomTimeFrame tfe, Map<Stats, Integer> params, NowPlayingArtist np) {
        CacheHandler cacheHandler = new CacheHandler();
        List<String> stringList = new ArrayList<>();
        boolean exception = false;
        String footer = "";
        if (enumSet.size() < 5) {
            footer = "Showing the following stats: " + enumSet.stream().map(Stats::toString).collect(Collectors.joining(" | ")) + "\n";
        }
        for (Stats stats : enumSet) {
            try {
                Integer param = params.get(stats);
                StatsCtx statsCtx = new StatsCtx(lastFMData, db, lastFM, userInfo, playsOnPeriod, timestamp, tfe, param, np);

                String process = cacheHandler.process(stats.cache, statsCtx);

                if (stats.mode.contains(AllMode.LINE_BREAK))
                    process += "\n";
                stringList.add(process);

            } catch (StatsCalculationException ex) {
                exception = true;
            }
        }
        if (!tfe.isAllTime()) {
            footer += "Showing stats" + tfe.getDisplayString() + "\n";
        }
        if (exception)
            footer += "An error ocurred on some statistics so there might be a few missing!";
        return new StatsResult(String.join("\n", stringList), footer);
    }

    public static String getListedName(Collection<Stats> modes) {
        return modes.stream().map(Stats::toString).collect(Collectors.joining(" | "));
    }

    public static Optional<Stats> parse(String singleWord) {
        String singular = singleWord.trim().replaceAll("[_\\s-]", "").toLowerCase(Locale.ROOT);
        String plural = singular
                .replaceAll("[sS]$", "").toLowerCase();

        return EnumSet.allOf(Stats.class).stream()
                .filter(z -> {
                            String strip = z.toString().replaceAll("[_\\s-]", "");
                            return strip.equalsIgnoreCase(singular) || strip.equalsIgnoreCase(plural) ||
                                   z.aliases.contains(singular) || z.aliases.contains(plural);
                        }
                )
                .findFirst();
    }

    public boolean hideInAll() {
        return this.mode.contains(AllMode.DONT);
    }

    public Set<String> getAliases() {

        return aliases;
    }

    public String toString() {
        return WordUtils.capitalizeFully(super.toString(), '-', '_').replaceAll("_", "-");
    }

    public String getHelpMessage() {
        return switch (this) {
            case JOINED -> "The date your account started";
            case PLAYS -> "Total number of scrobbles";
            case ARTISTS -> "Number of artists";
            case ALBUMS -> "Number of albums";
            case SONGS -> "Number of songs";
            case AVERAGES -> "How many albums/songs per artist/Album you listen to on average";
            case SCROBBLE_AVERAGES -> "How many times you listen a song/album/artist on average";
            case H_INDEX -> "Your hindex is the point where the number of plays you have of an artist at least matches its rank. For example, if your 56th ranked artist had at least 56 plays, but your 57th ranked artist has under 57, your hindex would be 56. Hindex is meant to quantify how many different artists you actively listen to.";
            case PERCENTAGE -> "How many artists are needed to obtain a % of your total scrobbles";
            case ALBUM_PERCENTAGE -> "How many albums are needed to obtain a % of your total scrobbles";
            case SONG_PERCENTAGE -> "How many songs are needed to obtain a % of your total scrobbles";
            case TOP -> "How many scrobbles your top x amounts for";
            case SUM_TOP -> "The percentage that the top x amounts for";
            case BREAKDOWNS -> "How many artist you have above certain breakpoints";
            case BREAKDOWNS_ALBUMS -> "How many albums you have above certain breakpoints";
            case BREAKDOWNS_SONGS -> "How many songs you have above certain breakpoints";
            case BREADTH_RATING -> "Shows your breadth rating. Your breadth rating is calculated from a number of different factors, and is an attempt to quantify your musical diversity. In general, this is what increases your breadth rating: - A higher hindex - More artists making up 50% of your scrobbles - Less scrobbles in your top 10 artists";
            case ARTISTS_OVER -> "How many artist you have above x plays";
            case ARTISTS_EQUAL -> "How many artist you have with x plays";
            case ARTISTS_UNDER -> "How many artist you have under x plays";
            case ALBUMS_OVER -> "How many albums you have over x plays";
            case ALBUMS_EQUAL -> "How many albums you have with x plays";
            case ALBUMS_UNDER -> "How many albums you have under x plays";
            case SONGS_OVER -> "How many songs you have above x plays";
            case SONGS_EQUAL -> "How many songs you have with x plays";
            case SONGS_UNDER -> "How many songs you have under x plays";
            case ARTIST_PERCENT -> "The percentage that a given artist amounts for";
            case ALBUM_PERCENT -> "The percentage that a given album amounts for";
            case TRACK_PERCENT -> "The percentage that a given song amounts for";
            case ARTIST_RANK -> "The rank of a given artist";
            case ALBUM_RANK -> "The rank of a given album";
            case SONG_RANK -> "The rank of a given song";
            case ALL -> "A overview with all modes. Equivalent to not specifying any stat";
        };

    }

    enum AllMode {
        NORMAL, DONT, LINE_BREAK
    }

    public record StatsResult(String description, String footer) {
    }
}


