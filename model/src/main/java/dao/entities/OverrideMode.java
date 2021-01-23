package dao.entities;

public enum OverrideMode {
    OVERRIDE("Override all reactions with the server reactions"), ADD("Adds server reactions before the user reactions"), ADD_END("Adds server reactions after the user reactions"), EMPTY("Only overrides if the user doesnt have any reaction set");

    private final String description;

    OverrideMode(String s) {
        description = s;
    }

    public String getDescription() {
        return description;
    }

}
