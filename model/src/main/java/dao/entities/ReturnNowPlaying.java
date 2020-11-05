package dao.entities;

import dao.utils.LinkUtils;

import java.awt.*;
import java.util.function.Consumer;

public class ReturnNowPlaying {
    private String artist;
    private long discordId;
    private String discordName;
    private Color roleColor;
    private String lastFMId;
    private int playNumber;
    public static final String WILDCARD = "|RETURNNOWPLAYINGWILDCARD|";
    String itemUrl;
    private Consumer<ReturnNowPlaying> displayer;

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

    public String toStringWildcard() {
        return ". " +
                "[" + LinkUtils.cleanMarkdownCharacter(discordName) + "](" + WILDCARD +
                ") - " +
                playNumber + " plays\n";
    }

    @Override
    public String toString() {
        if (itemUrl == null) {
            displayer.accept(this);
        }
        return ". " +
                "**[" + LinkUtils.cleanMarkdownCharacter(discordName) + "](" +
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

    public Consumer<ReturnNowPlaying> getDisplayer() {
        return displayer;
    }

    public void setDisplayer(Consumer<ReturnNowPlaying> displayer) {
        this.displayer = displayer;
    }
}
