package dao.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.TimeZone;

@JsonSerialize(using = UserExportSerializer.class)
public class UsersWrapper {
    private final Role role;
    private long discordID;
    private String lastFMName;
    private int timestamp;
    private final TimeZone timeZone;

    public UsersWrapper(long discordID, String lastFMName, Role role, TimeZone timeZone) {
        this.discordID = discordID;
        this.lastFMName = lastFMName;
        this.role = role;
        this.timeZone = timeZone;
    }

    public UsersWrapper(LastFMData lastFMData) {
        this.discordID = lastFMData.getDiscordId();
        this.lastFMName = lastFMData.getName();
        this.role = lastFMData.getRole();
        this.timeZone = lastFMData.getTimeZone();
    }

    UsersWrapper(long discordID, String lastFMName, int timestamp, Role role, TimeZone timeZone) {
        this.discordID = discordID;
        this.lastFMName = lastFMName;
        this.timestamp = timestamp;
        this.role = role;
        this.timeZone = timeZone;
    }

    public Role getRole() {
        return role;
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

    public @NotNull String getLastFMName() {
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

    public TimeZone getTimeZone() {
        return timeZone;
    }
}
