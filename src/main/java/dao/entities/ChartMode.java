package dao.entities;

public enum ChartMode {

    IMAGE("Using the standard image format"), IMAGE_INFO("Adds additional info to better identify the user of the chart"),
    IMAGE_ASIDE("Shows titles on the side of the chart"),
    IMAGE_ASIDE_INFO("Image Info and Aside Together"),
    LIST("Shows it on a embed"), PIE("Shows using a pie chart");

    private final String description;

    ChartMode(String s) {
        description = s;

    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return super.toString().replaceAll("_", "-");
    }
}
