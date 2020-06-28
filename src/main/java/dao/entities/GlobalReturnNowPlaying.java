package dao.entities;

import core.commands.CommandUtil;

import java.util.function.Consumer;
import java.util.function.Function;

public class GlobalReturnNowPlaying extends ReturnNowPlaying {
    private final PrivacyMode privacyMode;
    private String displayTitle = null;
    private Consumer<GlobalReturnNowPlaying> displayer;

    public GlobalReturnNowPlaying(long discordId, String lastFMId, String artist, int playNumber, PrivacyMode privacyMode) {
        super(discordId, lastFMId, artist, playNumber);
        this.privacyMode = privacyMode;
    }

    @Override
    public String toString() {
        String url;
        String discordName = CommandUtil.markdownLessString(getDiscordName());
        if (discordName.startsWith("Private User #")) {
            url = CommandUtil
                    .getLastFmArtistUserUrl(getArtist(), "chuu");
        } else {
            url = CommandUtil
                    .getLastFmArtistUserUrl(getArtist(), getLastFMId());
        }
        return ". " +
                "**[" + discordName + "](" +
                url +
                ")** - " +
                getPlayNumber() + " plays\n";
    }

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }


    public void setDisplayer(Consumer<GlobalReturnNowPlaying> displayer) {
        this.displayer = displayer;
    }

    @Override
    public void setDiscordName(String discordName) {
        displayTitle = discordName;
    }

    @Override
    public String getDiscordName() {
        if (displayTitle == null) {
            displayer.accept(this);
        }
        return displayTitle;
    }
}
