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

    public static String list(ChuuService dao, long guildId) {
        return ENUM_MAP.entrySet().stream().map(
                x -> {
                    String key = x.getKey();
                    switch (x.getValue()) {
                        case CROWNS_THRESHOLD:
                            int guildCrownThreshold = dao.getGuildCrownThreshold(guildId);
                            return String.format("**%s** -> %d", key, guildCrownThreshold);
                    }
                    return null;
                }).collect(Collectors.joining("\n"));

    }
}
