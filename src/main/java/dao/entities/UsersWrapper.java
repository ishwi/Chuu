package dao.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

@JsonSerialize(using = UserExportSerializer.class)
public class UsersWrapper {
    private long discordID;
    private String lastFMName;
    private int timestamp;

    public UsersWrapper(long discordID, String lastFMName) {
        this.discordID = discordID;
        this.lastFMName = lastFMName;
    }

    UsersWrapper(long discordID, String lastFMName, int timestamp) {
        this.discordID = discordID;
        this.lastFMName = lastFMName;
        this.timestamp = timestamp;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public long getDiscordID() {
        return discordID;
    }

    public void setDiscordID(long discordID) {
        this.discordID = discordID;
    }

    public String getLastFMName() {
        return lastFMName;
    }

    public void setLastFMName(String lastFMName) {
        this.lastFMName = lastFMName;
    }

    @Override
    public int hashCode() {
        int result = (int) (discordID ^ (discordID >>> 32));
        result = 31 * result + (lastFMName != null ? lastFMName.hashCode() : 0);
        result = 31 * result + timestamp;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsersWrapper that = (UsersWrapper) o;

        if (discordID != that.discordID) return false;
        if (timestamp != that.timestamp) return false;
        return Objects.equals(lastFMName, that.lastFMName);
    }
}
