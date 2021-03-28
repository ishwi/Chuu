package dao.entities;

import dao.utils.LinkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.function.Consumer;

public class GlobalStreakEntities extends StreakEntity {
    private final PrivacyMode privacyMode;
    private final long discordId;
    private final String lastfmId;
    private Consumer<GlobalStreakEntities> displayer;
    private String calculatedDisplayName;

    // TODO


    public GlobalStreakEntities(String currentArtist, int aCounter, String currentAlbum, int albCounter, String currentSong, int tCounter, Instant streakStart, PrivacyMode privacyMode, long discordId, String lastfmId) {
        super(currentArtist, aCounter, currentAlbum, albCounter, currentSong, tCounter, streakStart, null);
        this.privacyMode = privacyMode;
        this.discordId = discordId;
        this.lastfmId = lastfmId;
    }

    @NotNull
    public static String getComboString(String aString, StringBuilder description, int i, String currentArtist, int albCounter, String currentAlbum, int i2, String currentSong, @Nullable DateHolder start) {
        if (i > 1) {
            description.append("**Artist**: ")
                    .append(i).append(i >= 6000 ? "+" : "").append(" consecutive plays - ")
                    .append("**[").append(aString).append("](").append(LinkUtils.getLastFmArtistUrl(currentArtist)).append(")**").append("\n");
        }
        if (albCounter > 1 && currentAlbum != null && !currentAlbum.isBlank()) {
            description.append("**Album**: ")
                    .append(albCounter)
                    .append(albCounter >= 6000 ? "+" : "")
                    .append(" consecutive plays - ")
                    .append("**[").append(LinkUtils.cleanMarkdownCharacter(currentAlbum)).append("](")
                    .append(LinkUtils.getLastFmArtistAlbumUrl(currentArtist, currentAlbum)).append(")**")
                    .append("\n");
        }
        if (i2 > 1 && currentSong != null && !currentSong.isBlank()) {
            description.append("**Song**: ").append(i2).append(i2 >= 6000 ? "+" : "")
                    .append(" consecutive plays - ").append("**[")
                    .append(LinkUtils.cleanMarkdownCharacter(currentSong)).append("](").append(LinkUtils.getLastFMArtistTrack(currentArtist, currentSong)).append(")**").append("\n");
        }
        if (start != null) {
            description.append("**Started**: ").append("**[").append(start.date).append("](")
                    .append(start.link).append(")**")
                    .append("\n");
        }
        return description.toString() + "\n";
    }

    @Override
    public String toString() {
        String discordName = LinkUtils.markdownLessString(getName());
        GlobalStreakEntities combo = this;
        String aString = LinkUtils.cleanMarkdownCharacter(combo.getCurrentArtist());
        StringBuilder description = new StringBuilder("" + discordName + "\n");
        return getComboString(aString, description, combo.getaCounter(), combo.getCurrentArtist(), combo.getAlbCounter(), combo.getCurrentAlbum(), combo.gettCounter(), combo.getCurrentSong(), null);
    }

    public static record DateHolder(Instant start, String date, String link) {
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
