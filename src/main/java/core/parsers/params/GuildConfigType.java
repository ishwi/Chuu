package core.parsers.params;

import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.text.WordUtils;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum GuildConfigType {
    CROWNS_THRESHOLD("crowns"), CHART_MODE("chart"), WHOKNOWS_MODE("whoknows"), REMAINING_MODE("rest"), DELETE_MESSAGE("delete-message"), SHOW_DISABLED_WARNING("disabled-warning"),
    NP("np");
    static final Pattern npMode = Pattern.compile("((" + "CLEAR|" +
            EnumSet.allOf(NPMode.class).stream().filter(x -> !x.equals(NPMode.UNKNOWN)).map(NPMode::toString).collect(Collectors.joining("|")) +
            ")[ |&,]*)+", Pattern.CASE_INSENSITIVE);

    private static final Map<String, GuildConfigType> ENUM_MAP;
    private static final Pattern number = Pattern.compile("\\d+");

    static {
        ENUM_MAP = Stream.of(GuildConfigType.values())
                .collect(Collectors.toMap(GuildConfigType::getCommandName, Function.identity()));
    }

    private final String commandName;

    GuildConfigType(String command) {
        commandName = command;
    }

    public static GuildConfigType get(String name) {
        return ENUM_MAP.get(name);
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
                        case DELETE_MESSAGE:
                            boolean del = guildProperties.isDeleteMessages();
                            return String.format("**%s** -> %s", key, del);
                        case SHOW_DISABLED_WARNING:
                            boolean showWarnings = guildProperties.isShowWarnings();
                            return String.format("**%s** -> %s", key, showWarnings);
                        case NP:
                            EnumSet<NPMode> npModes = dao.getServerNPModes(guildId);
                            String strModes;
                            if (npModes.contains(NPMode.UNKNOWN)) {
                                strModes = "NOT SET";
                            } else {
                                strModes = NPMode.getListedName(npModes);
                            }
                            return String.format("**%s** -> %s", key, strModes);
                    }
                    return null;
                }).collect(Collectors.joining("\n"));

    }

    public String getCommandName() {
        return commandName;
    }

    public Predicate<String> getParser() {
        return switch (this) {
            case CROWNS_THRESHOLD -> number.asMatchPredicate();
            case CHART_MODE -> UserConfigType.chartMode.asMatchPredicate();
            case REMAINING_MODE, WHOKNOWS_MODE -> UserConfigType.whoknowsMode.asMatchPredicate();
            case DELETE_MESSAGE, SHOW_DISABLED_WARNING -> UserConfigType.bool.asMatchPredicate();
            case NP -> GuildConfigType.npMode.asMatchPredicate();
        };
    }

    public String getExplanation() {
        switch (this) {
            case CROWNS_THRESHOLD:
                return "A positive number that represent the minimum number of scrobbles for a crown to count";
            case CHART_MODE:
                String explanation = EnumSet.allOf(ChartMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                explanation += "\n\t\t\t**Clear**: Sets the default mode ";
                return "Set the mode for all charts of all users in this server. While this is set any user configuration will be overridden.\n" +
                        "\t\tThe possible values for the chart mode are the following:" + explanation;
            case WHOKNOWS_MODE:
                explanation = EnumSet.allOf(WhoKnowsMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                explanation += "\n\t\t\t**Clear**: Sets the default mode";
                return "Set the mode for all who knows of all users in this server. While this is set any user configuration will be overridden.\n" +
                        "\t\tThe possible values for the who knows mode are the following:" + explanation;
            case REMAINING_MODE:
                explanation = EnumSet.allOf(RemainingImagesMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                explanation += "\n\t\t\t**Clear**: Sets the default mode";
                return "Set the mode for all charts of the remaining images of the users in this server. While this is set any user configuration will be overridden \n" +
                        "\t\tThe possible values for the rest of the commands are the following:" + explanation;
            case DELETE_MESSAGE:
                return "Whether you want the bot to delete the original message the user wrote.";
            case SHOW_DISABLED_WARNING:
                return "Whether you want the bot to show a warning when you try to run a disabled command.";
            case NP:
                return "Setting this will alter the appearance of this server np commands. You can select up to 10 different from the following list and mix them up:\n" + "CLEAR | " + NPMode.getListedName(EnumSet.allOf(NPMode.class));
            default:
                return "";
        }
    }
}
