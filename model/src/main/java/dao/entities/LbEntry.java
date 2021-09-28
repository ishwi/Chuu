package dao.entities;

public abstract class LbEntry<T extends Number> {
    public static final String WILDCARD = "|template|to_be_replaced|";
    private final String lastFmId;
    private final long discordId;
    private final T entryCount;
    private String discordName;

    public LbEntry(String user, long discordId, T entryCount) {
        this.lastFmId = user;
        this.discordId = discordId;
        this.entryCount = entryCount;
    }

    public String getDiscordName() {
        return discordName;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public long getDiscordId() {
        return discordId;
    }

    public T getEntryCount() {
        return entryCount;
    }

    public String getLastFmId() {
        return lastFmId;
    }

    public abstract String toStringWildcard();


}

