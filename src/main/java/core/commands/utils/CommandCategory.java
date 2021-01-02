package core.commands.utils;

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
    RYM_BETA(11, "Testing command about a RYM integration, possibly broken"),
    MUSIC(12, "Play and scrobble music"),
    CONFIGURATION(13, "Personalize your bot usage"),
    MODERATION(14, "Command for bot admins"),
    SCROBBLING(15, "Commands for scrobbling");

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

