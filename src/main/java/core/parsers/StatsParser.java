package core.parsers;

import core.commands.Context;
import core.parsers.exceptions.InvalidDateException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.StatsParams;
import core.parsers.utils.CustomTimeFrame;
import core.util.stats.Stats;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
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
        String remaining = String.join(" ", Arrays.copyOfRange(words, 0, words.length));
        Pattern digit = Pattern.compile("^\\s*(\\d+).*");
        Pattern artistGx = Pattern.compile("(artist|album|song|track):(.*)");
        Matcher matcher = digit.matcher(remaining);

        Integer globalParam = null;
        if (matcher.matches()) {
            globalParam = Integer.valueOf(matcher.group(1));
            remaining = remaining.replaceAll(matcher.group(1), "");
        }

        var parsed = builder.apply(remaining);
        words = parsed.getRight();
        Set<StatsParam> building = parsed.getLeft();
        EnumSet<Stats> artistRank = EnumSet.of(Stats.ARTIST_RANK, Stats.ALBUM_RANK, Stats.SONG_RANK, Stats.ARTIST_PERCENT, Stats.ALBUM_PERCENTAGE, Stats.SONG_PERCENTAGE);
        EnumSet<Stats> modes = building.isEmpty() ? EnumSet.noneOf(Stats.class) : EnumSet.copyOf(building.stream().map(z -> z.mode).collect(Collectors.toSet()));
        modes.removeAll(artistRank);
        CustomTimeFrame tfe;
        NowPlayingArtist np = null;
        if (modes.isEmpty()) {
            ChartParserAux chartParserAux = new ChartParserAux(words, false);
            tfe = CustomTimeFrame.ofTimeFrameEnum(chartParserAux.parseTimeframe(TimeFrameEnum.ALL));
            words = chartParserAux.getMessage();
            np = parseArtist(words);
        } else {
            String artist = String.join(" ", words);
            Matcher artistMatcher = artistGx.matcher(artist);

            if (artistMatcher.find()) {
                np = parseArtist(artistMatcher.group(1).split("\\s+"));
                artist = artist.replace(artistMatcher.group(1), "");
                words = artist.split("\\s+");
            }
            ChartParserAux chartParserAux = new ChartParserAux(words);
            try {
                tfe = chartParserAux.parseCustomTimeFrame(TimeFrameEnum.ALL);
            } catch (InvalidDateException invalidDateException) {
                tfe = CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL);
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
        OptionData optionData2 = new OptionData(OptionType.STRING, "values", "the values to select");
        lines.stream().limit(25).forEach(t -> optionData2.addChoice(t, t));
        Explanation explanation = () -> new ExplanationLineType("help" + name, usage, OptionType.BOOLEAN);
        Explanation explanation2 = () -> new ExplanationLine("statistic ", join, optionData2);
        return List.of(explanation, explanation2);


    }

    public record StatsParam(Stats mode, Integer param) {

    }

}
