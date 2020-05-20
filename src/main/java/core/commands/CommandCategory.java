package core.commands;

import java.util.Comparator;
import java.util.StringJoiner;

public enum CommandCategory {
    STARTING(0, "Commands to get you started"),
    NOW_PLAYING(1, "Commands related to your current playing song"),
    CHARTS(2, "Collages about your musical preferences"),
    USER_STATS(3, "Stats about you"),
    SERVER_STATS(4, "Stats about a specific server"),
    BOT_STATS(5, "Stats considering all the bot users"),
    CROWNS(6, "Commands about who has listened the most a specific artist"),
    INFO(7, "Information about artists,albums..."),
    DISCOVERY(8, "Discover or help others discover new music"),
    BOT_INFO(9, "Information about the bot"),
    ARTIST_IMAGES(10, "Personalize the artist images displayed on the bot"),
    CONFIGURATION(11, "Personalize your bot usage"),
    MODERATION(12, "Command for bot admins");

    private final int order;
    private final String description;

    CommandCategory(int i, String s) {
        this.order = i;
        this.description = s;
    }

    public int getOrder() {
        return order;
    }

    public String getDescription() {
        return description;
    }


}

