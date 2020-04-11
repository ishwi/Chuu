package core.parsers.params;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum GuildConfigType {
    CROWNS_THRESHOLD("crowns");
    private static final Map<String, GuildConfigType> ENUM_MAP;
    private static final Pattern number = Pattern.compile("\\d+");

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

    public String getParamType() {
        switch (this) {
            case CROWNS_THRESHOLD:
                return Integer.class.getTypeName();
            default:
                return Object.class.getTypeName();
        }
    }

    public Predicate<String> getParser() {
        switch (this) {
            case CROWNS_THRESHOLD:
                return number.asMatchPredicate();
            default:
                return (s) -> true;
        }
    }

    public String getExplanation() {
        switch (this) {
            case CROWNS_THRESHOLD:
                return "A positive number that represent the minimum number of scrobbles for a crown to count";
            default:
                return "";
        }
    }
}
