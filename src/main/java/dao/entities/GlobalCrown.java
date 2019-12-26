package dao.entities;

public class GlobalCrown {
    private final String lastfmID;
    private final long discordId;
    private final int playcount;
    private final int ranking;


    public GlobalCrown(String lastfmID, long discordId, int playcount, int ranking) {
        this.lastfmID = lastfmID;
        this.discordId = discordId;
        this.playcount = playcount;
        this.ranking = ranking;
    }

    public String getLastfmID() {
        return lastfmID;
    }

    public long getDiscordId() {
        return discordId;
    }

    public int getPlaycount() {
        return playcount;
    }

    public int getRanking() {
        return ranking;
    }
}
