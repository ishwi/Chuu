package dao.entities;

import java.util.function.Supplier;

public class ReturnNowPlaying {
    private String artist;
    private long discordId;
    private String discordName;
    private String lastFMId;
    private long playNumber;
    private String memoized;
    private Supplier<String> generateString;
    private long index;

    public ReturnNowPlaying(long discordId, String lastFMId, String artist, long playNumber) {
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

    public long getIndex() {
        return index;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getLastFMId() {
        return lastFMId;
    }

    public void setLastFMId(String lastFMId) {
        this.lastFMId = lastFMId;
    }

    public long getPlayNumber() {
        return playNumber;
    }

    public void setPlayNumber(long playNumber) {
        this.playNumber = playNumber;
    }

    public void setIndex(long fakedIndex) {
        this.index = fakedIndex;
    }

    public void setMemoized(String memoized) {
        this.memoized = memoized;
    }

    public String toDisplay() {
        if (memoized == null) {
            memoized = generateString.get();
        }
        return memoized;
    }
//        return ". " +
//                "**[" + LinkUtils.cleanMarkdownCharacter(discordName) + "](" +
//                itemUrl +
//                ")** - " +
//                getPlayNumber() + " plays\n";


    public Supplier<String> getGenerateString() {
        return generateString;
    }

    public void setGenerateString(Supplier<String> generateString) {
        this.generateString = generateString;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
