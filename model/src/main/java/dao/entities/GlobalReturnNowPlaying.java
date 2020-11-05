package dao.entities;

import java.util.function.Consumer;

public class GlobalReturnNowPlaying extends ReturnNowPlaying {
    private final PrivacyMode privacyMode;
    private String displayTitle = null;
    private static final String USERWILDCARD = "|USER|WILDCARD|TO|REPLACE";
    private Consumer<GlobalReturnNowPlaying> globalDisplayer;

    public GlobalReturnNowPlaying(long discordId, String lastFMId, String artist, int playNumber, PrivacyMode privacyMode) {
        super(discordId, lastFMId, artist, playNumber);
        this.privacyMode = privacyMode;
    }

    @Override
    public String toStringWildcard() {
        return toString();
    }

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }


    public void setGlobalDisplayer(Consumer<GlobalReturnNowPlaying> displayer) {
        this.globalDisplayer = displayer;
    }

    @Override
    public void setDiscordName(String discordName) {
        displayTitle = discordName;
    }

    @Override
    public String getDiscordName() {
        if (displayTitle == null) {
            globalDisplayer.accept(this);
        }
        return displayTitle;
    }

    @Override
    public String toString() {
        if (itemUrl == null || displayTitle == null) {
            globalDisplayer.accept(this);
        }
        return ". " +
                "**[" + displayTitle + "](" +
                itemUrl +
                ")** - " +
                getPlayNumber() + " plays\n";
    }


    public String getItemUrl() {
        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
    }
}
