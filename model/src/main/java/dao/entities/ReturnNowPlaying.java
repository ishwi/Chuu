package dao.entities;

import java.awt.*;
import java.util.function.Supplier;

public class ReturnNowPlaying {
    private String artist;
    private long discordId;
    private String discordName;
    private Color roleColor;
    private String lastFMId;
    private int playNumber;
    private String memoized;
    String itemUrl;
    private Supplier<String> generateString;

    public ReturnNowPlaying(long discordId, String lastFMId, String artist, int playNumber) {
        this.discordId = discordId;
        this.lastFMId = lastFMId;
        this.artist = artist;
        this.playNumber = playNumber;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public String getDiscordName() {
        if (this.discordName == null) {
            memoized = generateString.get();
        }
        return discordName;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public Color getRoleColor() {
        return roleColor;
    }

    public void setRoleColor(Color roleColor) {
        this.roleColor = roleColor;
    }

    public String getArtist() {
        return artist;
    }

    public String getLastFMId() {
        return lastFMId;
    }

    public void setLastFMId(String lastFMId) {
        this.lastFMId = lastFMId;
    }


    public int getPlayNumber() {
        return playNumber;
    }

    public void setPlayNumber(int playNumber) {
        this.playNumber = playNumber;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setMemoized(String memoized) {
        this.memoized = memoized;
    }

    @Override
    public String toString() {
        if (memoized == null) {
            memoized = generateString.get();
        }
        return memoized;
//        return ". " +
//                "**[" + LinkUtils.cleanMarkdownCharacter(discordName) + "](" +
//                itemUrl +
//                ")** - " +
//                getPlayNumber() + " plays\n";
    }


    public Supplier<String> getGenerateString() {
        return generateString;
    }

    public void setGenerateString(Supplier<String> generateString) {
        this.generateString = generateString;
    }
}
