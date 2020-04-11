package core.parsers;

public class OptionalEntity {
    private final String value;
    private final String definition;
    private final boolean isEnabledByDefault;
    private final String blockedBy;

    public OptionalEntity(String value, String definition) {
        this(value, definition, false, null);
    }

    public OptionalEntity(String value, String definition, boolean isEnabledByDefault, String blockedBy) {
        this.value = value;
        this.definition = definition;
        this.isEnabledByDefault = isEnabledByDefault;
        this.blockedBy = blockedBy;
    }

    public String getDefinition() {
        return "\tCan use **" + value + "** to " + definition + "\n";
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
        if (!(o instanceof OptionalEntity)) {
            return false;
        }
        return this.value.equalsIgnoreCase(((OptionalEntity) o).value);
    }

    public String getValue() {
        return value;
    }

    public boolean isEnabledByDefault() {
        return isEnabledByDefault;
    }

    public String getBlockedBy() {
        return blockedBy;
    }
}
