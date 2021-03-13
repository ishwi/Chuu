package dao.entities;

public record RandomRating(long discordId, Byte rating, PrivacyMode privacyMode, String lastfmId) {


}
