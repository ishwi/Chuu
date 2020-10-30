package dao.entities;

public class ArtistUserPlays extends ArtistPlays {
    private final long discordId;

    public ArtistUserPlays(String name, int countA, long discordId) {
        super(name, countA);
        this.discordId = discordId;
    }

    public long getDiscordId() {
        return discordId;
    }

}
