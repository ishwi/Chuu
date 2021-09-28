package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.TimeframeExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.StatsParams;
import core.parsers.utils.CustomTimeFrame;
import core.util.stats.Stats;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NaturalTimeFrameEnum;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.WordUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StatsParser extends DaoParser<StatsParams> {
    private static final Pattern withParams = Pattern.compile("(.*)\\s+(\\d+)\\s*");
    public static final Function<String, Pair<Set<StatsParam>, String[]>> builder = (String value) -> {
        String[] split = value.trim().replaceAll("\\s+", " ").split("[|,&]+|(\\s(?!\\s*\\d*(\\s|$)))");
        Set<StatsParam> modes = new HashSet<>();
        List<String> words = new ArrayList<>();
        for (String mode : split) {

            Matcher matcher = withParams.matcher(mode);
            Integer param = null;
            String mapped = mode;
            if (matcher.matches()) {
                mapped = matcher.group(1);
                param = Integer.valueOf(matcher.group(2));
            }
            Integer finalParam = param;
            Stats.parse(mapped)
                    .ifPresentOrElse(s -> modes.add(new StatsParam(s, finalParam)), () -> words.add(mode));
        }
        return Pair.of(modes, words.toArray(new String[0]));
    };


    public StatsParser(ChuuService db) {
        super(db);
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected StatsParams parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        Set<StatsParser.StatsParam> building = new LinkedHashSet<>();
        ParserAux parserAux = new ParserAux(words);
        User oneUser = parserAux.getOneUserPermissive(e, dao);
        LastFMData data = findLastfmFromID(oneUser, e);
        words = parserAux.getMessage();
        if (words.length == 0) {
            return new StatsParams(e, building, data, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL));
        }
        String command = words[0];
        if (command.equalsIgnoreCase("help")) {
            if (words.length > 1) {
                if (words[1].equals("all")) {
                    building = EnumSet.allOf(Stats.class).stream().map(z -> new StatsParam(z, null)).collect(Collectors.toSet());
                } else {
                    String remaining = String.join(" ", Arrays.copyOfRange(words, 1, words.length));
                    Pair<Set<StatsParam>, String[]> parser = builder.apply(remaining);
                    building = parser.getLeft();
                }
            } else {
                building = EnumSet.allOf(Stats.class).stream().map(z -> new StatsParam(z, null)).collect(Collectors.toSet());
            }
            return new StatsParams(e, building, true, false, data);
        } else if (command.equalsIgnoreCase("list")) {
            return new StatsParams(e, building, false, data, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL), null, false, null);
        } else {
            return parse(words, e, data);
        }
    }

    private StatsParams parse(String[] words, Context e, LastFMData data) {
        ChartParserAux chartParserAux = new ChartParserAux(words);
        CustomTimeFrame tfe = new CustomTimeFrame(chartParserAux.parseNaturalTimeFrame(NaturalTimeFrameEnum.ALL), 1);

        words = chartParserAux.getMessage();
        String remaining = String.join(" ", Arrays.copyOfRange(words, 0, words.length));

        Pattern digit = Pattern.compile("^\\s*(\\d+)");
        Pattern artistGx = Pattern.compile("(artist|album|song|track):(.*)");
        Matcher matcher = digit.matcher(remaining);

        Integer globalParam = null;
        if (matcher.find()) {
            globalParam = Integer.valueOf(matcher.group(1));
            remaining = remaining.replaceAll(matcher.group(1), "");
        }

        var parsed = builder.apply(remaining);
        words = parsed.getRight();
        Set<StatsParam> building = parsed.getLeft();
        EnumSet<Stats> artistRank = EnumSet.of(Stats.ARTIST_RANK, Stats.ALBUM_RANK, Stats.SONG_RANK, Stats.ARTIST_PERCENT, Stats.ALBUM_PERCENTAGE, Stats.SONG_PERCENTAGE);
        EnumSet<Stats> modes = building.isEmpty() ? EnumSet.noneOf(Stats.class) : EnumSet.copyOf(building.stream().map(z -> z.mode).collect(Collectors.toSet()));
        modes.removeAll(artistRank);
        NowPlayingArtist np = null;
        if (modes.isEmpty() && !building.isEmpty()) {
            remaining = String.join(" ", words);
            np = parseArtist(words);
        } else {

            String artist = String.join(" ", words);
            Matcher artistMatcher = artistGx.matcher(artist);

            if (artistMatcher.find()) {
                np = parseArtist(artistMatcher.group(2).split("\\s+"));
            }

        }
        if (!remaining.isBlank() && building.isEmpty()) {
            return new StatsParams(e, building, true, data, tfe, globalParam, true, np);
        }

        return new StatsParams(e, building, false, data, tfe, globalParam, false, np);
    }

    private NowPlayingArtist parseArtist(String[] words) {
        String regex = "(?<!\\\\)\\s*-\\s*";
        String[] content = String.join(" ", words).split(regex);
        content = Arrays.stream(content).filter(Predicate.not(StringUtils::isBlank)).toArray(String[]::new);
        String artist = null;
        String song = null;
        String album = null;
        if (content.length == 0) {
            return null;
        }
        if (content.length == 1) {
            artist = content[0];
        }
        if (content.length == 2) {
            artist = content[0];
            song = content[1];
            StringBuilder albumStrb = null;
            String[] split = song.split("\\|");
            if (split.length > 1) {
                song = split[0];
                albumStrb = new StringBuilder();
                for (int i = 1; i < split.length; i++) {
                    albumStrb.append(split[i]);
                    if (i != split.length - 1) {
                        albumStrb.append(" ");
                    }
                }
            }
            album = albumStrb != null ? albumStrb.toString() : null;
        }
        if (content.length > 2) {
            return null;
        }
        return new NowPlayingArtist(artist, null, true, album, song, null, null, true);
    }

    @Override
    public List<Explanation> getUsages() {
        EnumSet<Stats> set = EnumSet.allOf(Stats.class);
        List<String> lines = set.stream().map(x -> WordUtils.capitalizeFully(x.name().replaceAll("_", "-"), '-')).toList();
        String join = String.join("** | **", lines);
        String name = "statistics";
        String usage = "\t Writing **__help__** will give you a brief description of all the " + name + " that you include in the command\n";

        OptionData stats = new OptionData(OptionType.STRING, "stats-select", "the values to select");
        OptionData statsFree = new OptionData(OptionType.STRING, "stats-text", "the values to select");
        lines.stream().limit(25).forEach(t -> stats.addChoice(t, t));

        Explanation help = () -> new ExplanationLineType("help", usage, OptionType.BOOLEAN);

        Explanation statistics = () -> new ExplanationLine("statistics", join, List.of(stats, statsFree));


        OptionData artist = new OptionData(OptionType.STRING, "artist", "The artist for some modes");
        OptionData song = new OptionData(OptionType.STRING, "song", "The song for some modes");
        OptionData album = new OptionData(OptionType.STRING, "album", "The album for some modes");
        Explanation artists = () -> new ExplanationLine("artist - song | album ", "Some modes accept ", List.of(artist, song, album));

        return List.of(statistics, help, artists, new TimeframeExplanation(TimeFrameEnum.ALL),
                () -> new ExplanationLineType("modifier", "A number that modifies all the stats", OptionType.INTEGER));


    }

    @Override
    public StatsParams parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        User user = InteractionAux.parseUser(e);
        TimeFrameEnum tfe = InteractionAux.parseTimeFrame(e, TimeFrameEnum.ALL);
        String artist = Optional.ofNullable(e.getOption("artist")).map(OptionMapping::getAsString).orElse(null);
        String album = Optional.ofNullable(e.getOption("album")).map(OptionMapping::getAsString).orElse(null);
        String song = Optional.ofNullable(e.getOption("song")).map(OptionMapping::getAsString).orElse(null);
        Long param = Optional.ofNullable(e.getOption("modifier")).map(OptionMapping::getAsLong).orElse(null);
        String stats = Optional.ofNullable(e.getOption("stats-select")).map(OptionMapping::getAsString).orElse("");
        String statsText = stats + " " + Optional.ofNullable(e.getOption("stats-text")).map(OptionMapping::getAsString).orElse("");
        boolean help = Optional.ofNullable(e.getOption("help")).map(OptionMapping::getAsBoolean).orElse(false);
        LastFMData data = findLastfmFromID(user, ctx);
        Set<StatsParam> apply = builder.apply(statsText).getLeft();
        apply = help && apply.isEmpty() ? EnumSet.allOf(Stats.class).stream().map(z -> new StatsParam(z, null)).collect(Collectors.toSet()) : apply;
        return new StatsParams(ctx, apply, help, data, CustomTimeFrame.ofTimeFrameEnum(tfe), param != null ? Math.toIntExact(param) : null, apply.isEmpty());
    }


    public record StatsParam(Stats mode, Integer param) {

    }

}
