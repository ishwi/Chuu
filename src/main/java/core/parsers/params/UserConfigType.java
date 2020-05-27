package core.parsers.params;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.ChartMode;
import dao.entities.LastFMData;
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

public enum UserConfigType {

    CHART_MODE("chart"), NOTIFY_IMAGE("image-notify"), PRIVATE_UPDATE("private-update"), WHOKNOWS_MODE("whoknows"), REMAINING_MODE("rest");

    private static final Map<String, UserConfigType> ENUM_MAP;
    private static final Pattern bool = Pattern.compile("(True|False)", Pattern.CASE_INSENSITIVE);
    private static final Pattern chartMode = Pattern.compile("(Image|Image-info|Pie|List|Clear)", Pattern.CASE_INSENSITIVE);
    private static final Pattern whoknowsMode = Pattern.compile("(Image|Pie|List|Clear)", Pattern.CASE_INSENSITIVE);


    static {
        ENUM_MAP = Stream.of(UserConfigType.values())
                .collect(Collectors.toMap(UserConfigType::getCommandName, Function.identity()));
    }

    private final String commandName;
    private Type paramType;

    UserConfigType(String command) {
        commandName = command;
    }

    public static UserConfigType get(String name) {
        return ENUM_MAP.get(name);
    }

    public String getCommandName() {
        return commandName;
    }


    public Predicate<String> getParser() {
        switch (this) {
            case CHART_MODE:
                return chartMode.asMatchPredicate();
            case REMAINING_MODE:
            case WHOKNOWS_MODE:
                return whoknowsMode.asMatchPredicate();

            case PRIVATE_UPDATE:
            case NOTIFY_IMAGE:
                return bool.asMatchPredicate();
            default:
                return (s) -> true;

        }
    }

    public String getExplanation() {
        switch (this) {
            case PRIVATE_UPDATE:
                return "If you want others users to be able to update your account with a ping and the update command (true for making it private, false for it to be public)";
            case NOTIFY_IMAGE:
                return "Whether you will get notified or not when a submitted image gets accepted (true = notify, false = no)";
            case CHART_MODE:
                String collect = EnumSet.allOf(ChartMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                collect += "\n\t\t\t**Clear**: Sets the default mode";
                return "Set the mode for all charts. " +
                        "Keep in mind that if a server has a set value that will be prioritized.\n" +
                        "\t\tThe possible values for the chart mode are the following:" + collect;
            case WHOKNOWS_MODE:
                collect = EnumSet.allOf(WhoKnowsMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                collect += "\n\t\t\t**Clear**: Sets the default mode";
                return "Set the mode for all charts. " +
                        "Keep in mind that if a server has a set value that will be prioritized.\n" +
                        "\t\tThe possible values for the who knows mode are the following:" + collect;
            case REMAINING_MODE:
                collect = EnumSet.allOf(RemainingImagesMode.class).stream().map(x -> "\n\t\t\t**" + WordUtils.capitalizeFully(x.toString()) + "**: " + x.getDescription()).collect(Collectors.joining(""));
                collect += "\n\t\t\t**Clear**: Sets the default mode";
                return "Set the mode for the rest of the commands. " +
                        "Keep in mind that if a server has a set value that will be prioritized.\n" +
                        "\t\tThe possible values for the rest of the commands are the following:" + collect;
            default:
                return "";
        }
    }

    public static String list(ChuuService dao, long discordId) {
        return ENUM_MAP.entrySet().stream().map(
                x -> {
                    String key = x.getKey();
                    LastFMData lastFMData;
                    try {
                        lastFMData = dao.findLastFMData(discordId);
                    } catch (InstanceNotFoundException e) {
                        lastFMData = null;
                    }

                    switch (x.getValue()) {
                        case PRIVATE_UPDATE:
                            boolean privateUpdate = lastFMData != null && lastFMData.isPrivateUpdate();
                            return String.format("**%s** -> %s", key, privateUpdate);
                        case NOTIFY_IMAGE:
                            privateUpdate = lastFMData != null && lastFMData.isImageNotify();
                            return String.format("**%s** -> %s", key, privateUpdate);
                        case CHART_MODE:
                            String chartMode;
                            if (lastFMData == null || lastFMData.getChartMode() == null) {
                                chartMode = "NOT SET";
                            } else {
                                chartMode = lastFMData.getChartMode().toString();
                            }
                            return String.format("**%s** -> %s", key, chartMode);
                        case WHOKNOWS_MODE:
                            String whoknowsmode;
                            if (lastFMData == null || lastFMData.getWhoKnowsMode() == null) {
                                whoknowsmode = "NOT SET";
                            } else {
                                whoknowsmode = lastFMData.getWhoKnowsMode().toString();
                            }
                            return String.format("**%s** -> %s", key, whoknowsmode);
                        case REMAINING_MODE:
                            String remaining;
                            if (lastFMData == null || lastFMData.getRemainingImagesMode() == null) {
                                remaining = "NOT SET";
                            } else {
                                remaining = lastFMData.getRemainingImagesMode().toString();
                            }
                            return String.format("**%s** -> %s", key, remaining);
                    }
                    return null;
                }).

                collect(Collectors.joining("\n"));

    }
}

