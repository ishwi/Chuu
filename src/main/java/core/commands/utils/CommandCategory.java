package core.commands.utils;

import java.util.Locale;

public enum CommandCategory {
    STARTING(0, "Commands to get you started"),
    NOW_PLAYING(1, "Commands related to your current playing song"),
    CHARTS(2, "Collages about your musical preferences"),
    WHO_KNOWS(3, "Who knows a song/album/artist?", "wk"),
    USER_STATS(4, "Stats about you"),
    SERVER_STATS(5, "Stats about a specific server", "server"),
    BOT_STATS(6, "Stats considering all the bot users", "global"),
    CROWNS(7, "Who has listened the most an artist"),
    UNIQUES(8, "Commands about unique artists"),
    INFO(9, "Information about artists,albums..."),
    DISCOVERY(10, "Discover or help others discover new music"),
    BOT_INFO(11, "Information about the bot"),
    LOVE(12, "Using the love feature of last.fm"),
    ARTIST_IMAGES(13, "Personalize the artist images displayed on the bot"),
    RYM(14, "Testing command about a RYM integration, possibly broken"),
    TRENDS(15, "Weekly stats about server/global trends"),
    STREAKS(16, "Tracking your combos"),
    MUSIC(17, "Play and scrobble music"),
    SERVER_LEADERBOARDS(18, "Server leaderboards", "leaderboard"),
    CONFIGURATION(19, "Personalize your bot usage"),
    MODERATION(20, "Command for bot admins"),
    SCROBBLING(21, "Commands for scrobbling");

    private final int order;
    private final String description;
    private final String prefix;

    CommandCategory(int i, String s) {
        this.order = i;
        this.description = s;
        this.prefix = this.name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
    }

    CommandCategory(int i, String s, String prefix) {
        this.order = i;
        this.description = s;
        this.prefix = prefix;
    }

    public int getOrder() {
        return order;
    }

    public String getDescription() {
        return description;
    }

    public String getPrefix() {
        return prefix;
    }
}

