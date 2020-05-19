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

public enum UserConfigType {

    PRIVATE_UPDATE("private-update");

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
                return bool.asMatchPredicate();
            default:
                return (s) -> true;
        }
    }

    public String getExplanation() {
        switch (this) {
            case PRIVATE_UPDATE:
                return "If you want others users to be able to update your account with a ping and the update command (true for making it private, false for it to be public)";
            default:
                return "";
        }
    }

    public static String list(ChuuService dao, long discordId) {
        return ENUM_MAP.entrySet().stream().map(
                x -> {
                    String key = x.getKey();
                    switch (x.getValue()) {
                        case PRIVATE_UPDATE:
                            try {
                                boolean privateUpdate = dao.findLastFMData(discordId).isPrivateUpdate();
                                return String.format("**%s** -> %s", key, privateUpdate);

                            } catch (InstanceNotFoundException e) {
                                return String.format("**%s** -> %s", key, false);
                            }
                    }
                    return null;
                }).collect(Collectors.joining("\n"));

    }
}

