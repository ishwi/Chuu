package dao.entities;

public enum OverrideColorMode {
    OVERRIDE("Override all colors with the server colors"), EMPTY("Only overrides if the user doesnt have any color set");

    private final String description;

    OverrideColorMode(String s) {
        description = s;
    }

    public String getDescription() {
        return description;
    }

}
