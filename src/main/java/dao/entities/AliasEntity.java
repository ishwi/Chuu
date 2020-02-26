package dao.entities;

import java.time.LocalDate;
import java.util.Objects;

public class AliasEntity {
    private final long id;
    private final String alias;
    private final long artistId;
    private final long discorId;
    private final LocalDate dateTime;
    private String artistName;

    public AliasEntity(long id, String alias, long artistId, long discorId, LocalDate dateTime, String artistName) {
        this.id = id;
        this.alias = alias;
        this.artistId = artistId;
        this.discorId = discorId;
        this.dateTime = dateTime;
        this.artistName = artistName;
    }

    public LocalDate getDateTime() {
        return dateTime;
    }

    public long getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public long getArtistId() {
        return artistId;
    }

    public long getDiscorId() {
        return discorId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AliasEntity that = (AliasEntity) o;
        return id == that.id &&
               artistId == that.artistId &&
               discorId == that.discorId &&
               Objects.equals(alias, that.alias) &&
               Objects.equals(dateTime, that.dateTime) &&
               Objects.equals(artistName, that.artistName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, alias, artistId, discorId, dateTime, artistName);
    }
}
