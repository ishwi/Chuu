package core.parsers.params;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum GuildConfigType {
    CROWNS_THRESHOLD("crowns"), ADDITIONAL_CHART_INFO("chart-info");

    private static final Map<String, GuildConfigType> ENUM_MAP;
    private static final Pattern number = Pattern.compile("\\d+");
    private static final Pattern bool = Pattern.compile("(True|False)", Pattern.CASE_INSENSITIVE);


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
            case ADDITIONAL_CHART_INFO:
                return bool.asMatchPredicate();
            default:
                return (s) -> true;
        }
    }

    public String getExplanation() {
        switch (this) {
            case CROWNS_THRESHOLD:
                return "A positive number that represent the minimum number of scrobbles for a crown to count";
            case ADDITIONAL_CHART_INFO:
                return "Whether you want the chart images in the server to have additional info to better identify each image. Keep in mind that users can override this setting for themselves on their own later on.";
            default:
                return "";
        }
    }

    public static String list(ChuuService dao, long guildId) {
        return ENUM_MAP.entrySet().stream().map(
                x -> {
                    String key = x.getKey();
                    switch (x.getValue()) {
                        case CROWNS_THRESHOLD:
                            int guildCrownThreshold = dao.getGuildCrownThreshold(guildId);
                            return String.format("**%s** -> %d", key, guildCrownThreshold);
                        case ADDITIONAL_CHART_INFO:
                            boolean guildEmbedConfig = dao.getGuildEmbedConfig(guildId);
                            return String.format("**%s** -> %s", key, guildEmbedConfig);
                    }
                    return null;
                }).collect(Collectors.joining("\n"));

    }
}
