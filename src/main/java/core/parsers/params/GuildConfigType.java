package core.parsers.params;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.ChartMode;
import dao.entities.GuildProperties;
import dao.entities.RemainingImagesMode;
import dao.entities.WhoKnowsMode;
import org.apache.commons.text.WordUtils;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum GuildConfigType {
    CROWNS_THRESHOLD("crowns"), CHART_MODE("chart"), WHOKNOWS_MODE("whoknows"), REMAINING_MODE("rest");


    private static final Map<String, GuildConfigType> ENUM_MAP;
    private static final Pattern number = Pattern.compile("\\d+");
    private static final Pattern bool = Pattern.compile("(True|False)", Pattern.CASE_INSENSITIVE);
    private static final Pattern chartMode = Pattern.compile("(Image|Image-info|Pie|List|Clear)", Pattern.CASE_INSENSITIVE);
    private static final Pattern whoknowsMode = Pattern.compile("(Image|Pie|List|Clear)", Pattern.CASE_INSENSITIVE);

    static {
        ENUM_MAP = Stream.of(GuildConfigType.values())
                .collect(Collectors.toMap(GuildConfigType::getCommandName, Function.identity()));
    }

    private final String commandName;
    private Type paramType;

    GuildConfigType(String command) {
        commandName = command;
    }

    public static GuildConfigType get(String name) {
        return ENUM_MAP.get(name);
    }

    public String getCommandName() {
        return commandName;
    }


    public Predicate<String> getParser() {
        switch (this) {
            case CROWNS_THRESHOLD:
                return number.asMatchPredicate();
            case CHART_MODE:
                return chartMode.asMatchPredicate();
            case REMAINING_MODE:
            case WHOKNOWS_MODE:
                return whoknowsMode.asMatchPredicate();
            default:
                return (s) -> true;
        }
    }

    public String getExplanation() {
        switch (this) {
            case CROWNS_THRESHOLD:
                return "A positive number that represent the minimum number of scrobbles for a crown to count";
            case CHART_MODE:
                String collect = EnumSet.allOf(ChartMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                collect += "\n\t\t\t**Clear**: Sets the default mode ";
                return "Set the mode for all charts of all users in this server. While this is set any user configuration will be overridden.\n" +
                        "\t\tThe possible values for the chart mode are the following:" + collect;
            case WHOKNOWS_MODE:
                collect = EnumSet.allOf(WhoKnowsMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                collect += "\n\t\t\t**Clear**: Sets the default mode";
                return "Set the mode for all who knows of all users in this server. While this is set any user configuration will be overridden.\n" +
                        "\t\tThe possible values for the who knows mode are the following:" + collect;
            case REMAINING_MODE:
                collect = EnumSet.allOf(RemainingImagesMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                collect += "\n\t\t\t**Clear**: Sets the default mode";
                return "Set the mode for all charts of the remaining images of the users in this server. While this is set any user configuration will be overridden \n" +
                        "\t\tThe possible values for the rest of the commands are the following:" + collect;
            default:
                return "";
        }
    }

    public static String list(ChuuService dao, long guildId) {
        GuildProperties guildProperties;
        try {
            guildProperties = dao.getGuildProperties(guildId);
        } catch (InstanceNotFoundException e) {
            return "This Server has no registered users?!";
        }
        return ENUM_MAP.entrySet().stream().map(
                x -> {
                    String key = x.getKey();
                    switch (x.getValue()) {
                        case CROWNS_THRESHOLD:
                            int guildCrownThreshold = guildProperties.getCrown_threshold();
                            return String.format("**%s** -> %d", key, guildCrownThreshold);
                        case CHART_MODE:
                            ChartMode chartMode = guildProperties.getChartMode();
                            String mode;
                            if (chartMode == null) {
                                mode = "NOT SET";
                            } else {
                                mode = chartMode.toString();
                            }
                            return String.format("**%s** -> %s", key, mode);
                        case WHOKNOWS_MODE:
                            String whoknowsmode;
                            WhoKnowsMode modes = guildProperties.getWhoKnowsMode();

                            if (modes == null) {
                                whoknowsmode = "NOT SET";
                            } else {
                                whoknowsmode = modes.toString();
                            }
                            return String.format("**%s** -> %s", key, whoknowsmode);
                        case REMAINING_MODE:
                            RemainingImagesMode modes2 = guildProperties.getRemainingImagesMode();

                            if (modes2 == null) {
                                whoknowsmode = "NOT SET";
                            } else {
                                whoknowsmode = modes2.toString();
                            }
                            return String.format("**%s** -> %s", key, whoknowsmode);
                    }
                    return null;
                }).collect(Collectors.joining("\n"));

    }
}
