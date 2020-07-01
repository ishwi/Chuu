package dao.entities;

import core.commands.CommandUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.function.Consumer;

public class GlobalStreakEntities extends StreakEntity {
    private final PrivacyMode privacyMode;
    private final long discordId;
    private final String lastfmId;
    private Consumer<GlobalStreakEntities> displayer;
    private String calculatedDisplayName;


    public GlobalStreakEntities(String currentArtist, int aCounter, String currentAlbum, int albCounter, String currentSong, int tCounter, Instant streakStart, PrivacyMode privacyMode, long discordId, String lastfmId) {
        super(currentArtist, aCounter, currentAlbum, albCounter, currentSong, tCounter, streakStart);
        this.privacyMode = privacyMode;
        this.discordId = discordId;
        this.lastfmId = lastfmId;
    }

    @Override
    public String toString() {
        String discordName = CommandUtil.markdownLessString(getName());
        GlobalStreakEntities combo = this;
        String aString = CommandUtil.cleanMarkdownCharacter(combo.getCurrentArtist());
        StringBuilder description = new StringBuilder("" + discordName + "\n");
        return getComboString(aString, description, combo.getaCounter(), combo.getCurrentArtist(), combo.getAlbCounter(), combo.getCurrentAlbum(), combo.gettCounter(), combo.getCurrentSong());
    }

    @NotNull
    public static String getComboString(String aString, StringBuilder description, int i, String currentArtist, int albCounter, String currentAlbum, int i2, String currentSong) {
        if (i > 1) {
            description.append("**Artist**: ")
                    .append(i).append(i >= 5050 ? "+" : "").append(" consecutive plays - ")
                    .append("**[").append(aString).append("](").append(CommandUtil.getLastFmArtistUrl(currentArtist)).append(")**").append("\n");
        }
        if (albCounter > 1) {
            description.append("**Album**: ")
                    .append(albCounter)
                    .append(albCounter >= 5050 ? "+" : "")
                    .append(" consecutive plays - ")
                    .append("**[").append(CommandUtil.cleanMarkdownCharacter(currentAlbum)).append("](")
                    .append(CommandUtil.getLastFmArtistAlbumUrl(currentArtist, currentAlbum)).append(")**")
                    .append("\n");
        }
        if (i2 > 1) {
            description.append("**Song**: ").append(i2).append(i2 >= 5050 ? "+" : "")
                    .append(" consecutive plays - ").append("**[")
                    .append(CommandUtil.cleanMarkdownCharacter(currentSong)).append("](").append(CommandUtil.getLastFMArtistTrack(currentArtist, currentSong)).append(")**").append("\n");
        }
        return description.toString() + "\n";
    }

    public void setDisplayer(Consumer<GlobalStreakEntities> displayer) {
        this.displayer = displayer;
    }


    public String getName() {
        if (calculatedDisplayName == null) {
            displayer.accept(this);
        }
        return calculatedDisplayName;
    }

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }

    public long getDiscordId() {
        return discordId;
    }

    public String getLastfmId() {
        return lastfmId;
    }

    public Consumer<GlobalStreakEntities> getDisplayer() {
        return displayer;
    }

    public String getCalculatedDisplayName() {
        return calculatedDisplayName;
    }

    public void setCalculatedDisplayName(String calculatedDisplayName) {
        this.calculatedDisplayName = calculatedDisplayName;
    }
}
