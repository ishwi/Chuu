package dao.entities;

public record Rating(long discordId, Byte rating, boolean isSameGuild) {
}
