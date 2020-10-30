package dao.entities;

public enum RemainingImagesMode {
    IMAGE("Using the standard image format"), LIST("Shows it on a embed"), PIE("Shows using a pie chart");

    private final String description;

    RemainingImagesMode(String s) {
        description = s;
    }

    public String getDescription() {
        return description;
    }
}
