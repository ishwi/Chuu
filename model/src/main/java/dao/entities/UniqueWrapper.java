package dao.entities;

import java.util.List;

public class UniqueWrapper<T> {
    private final int rows;
    private final long discordId;
    private final String lastFmId;
    private final List<T> uniqueData;

    public UniqueWrapper(int rows, long discordId, String lastFmId, List<T> uniqueData) {
        this.rows = rows;
        this.discordId = discordId;
        this.lastFmId = lastFmId;
        this.uniqueData = uniqueData;
    }

    public int getRows() {

        return rows;
    }

    public long getDiscordId() {
        return discordId;
    }

    public String getLastFmId() {
        return lastFmId;
    }

    public List<T> getUniqueData() {
        return uniqueData;
    }
}
