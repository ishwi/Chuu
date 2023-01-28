package core.parsers.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record OptionalEntity(String value, String definition, boolean isEnabledByDefault, Set<String> blockedBy,
                             Set<String> aliases) {
    private static final Pattern options = Pattern.compile("(?:--|~~|—)(\\w+)");


    public OptionalEntity(String value, String definition) {
        this(value, definition, false, Collections.emptySet(), Collections.emptySet());
    }

    public OptionalEntity(String value, String definition, String... aliases) {
        this(value, definition, false, Collections.emptySet(), Set.of(aliases));
    }


    public OptionalEntity(String value, String definition, boolean isEnabledByDefault, String blockedBy) {
        this(value, definition, isEnabledByDefault, Set.of(blockedBy), Collections.emptySet());
    }

    public static boolean isWordAValidOptional(Set<OptionalEntity> optPool, Map<String, OptionalEntity> aliases, String toTest) {
        Matcher matcher = options.matcher(toTest);

        if (matcher.matches()) {
            String alias = matcher.group(1);
            return optPool.contains(new OptionalEntity(alias, "")) || aliases.containsKey(alias);
        }
        return false;
    }

    /**
     * @param valid      Needs to be a previously validated with {@link #options}
     * @param opts       List of valid optionals for this parsers
     * @param optAliases Map of aliases to valid optionals
     * @return the substring without the optional prefixes
     */
    //Valid needs to
    public static String getOptPartFromValid(@NotNull String valid, Set<OptionalEntity> opts, Map<String, OptionalEntity> optAliases) {

        Matcher matcher = options.matcher(valid);
        if (matcher.matches()) {
            String entity = matcher.group(1);
            if (opts.contains(new OptionalEntity(entity, "")))
                return entity;
            else {
                OptionalEntity optionalEntity = optAliases.get(entity);
                assert optionalEntity != null;
                return optionalEntity.value;
            }
        }
        throw new IllegalStateException();
    }

    public String getDescription() {
        return "\t Can use **" + "--" + value + "** to " + definition + "\n";
    }


    public OptionalEntity withDescription(String desc) {
        return new OptionalEntity(value, desc, isEnabledByDefault, blockedBy, aliases);
    }

    public OptionalEntity withBlockedBy(String... blockedBy) {
        return new OptionalEntity(value, definition, true, Set.of(blockedBy), aliases);
    }

    public OptionalEntity withAlias(String alias) {
        return new OptionalEntity(value, definition, isEnabledByDefault, blockedBy, Stream.concat(aliases.stream(), Stream.of(alias)).collect(Collectors.toSet()));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof OptionalEntity oe) {
            return this.value.equalsIgnoreCase(oe.value) || this.aliases.contains(oe.value);
        } else {
            return false;
        }
    }


}
