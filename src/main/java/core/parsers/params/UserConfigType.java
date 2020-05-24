package core.parsers.params;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UserConfigType {

    PRIVATE_UPDATE("private-update"), NOTIFY_IMAGE("image-notify"), ADDITIONAL_CHART_INFO("chart-info");

    private static final Map<String, UserConfigType> ENUM_MAP;
    private static final Pattern bool = Pattern.compile("(True|False)", Pattern.CASE_INSENSITIVE);

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
            case PRIVATE_UPDATE:
            case NOTIFY_IMAGE:
            case ADDITIONAL_CHART_INFO:
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
            case ADDITIONAL_CHART_INFO:
                return "Whether you want the chart images for the user to have additional info to better identify each image. Keep in mind that server can modify the default value for this.";
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
                        case ADDITIONAL_CHART_INFO:
                            privateUpdate = lastFMData != null && lastFMData.isAdditionalEmbedChart();
                            return String.format("**%s** -> %s", key, privateUpdate);
                    }
                    return null;
                }).

                collect(Collectors.joining("\n"));

    }
}

