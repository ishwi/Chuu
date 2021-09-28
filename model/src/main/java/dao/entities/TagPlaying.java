package dao.entities;

public class TagPlaying extends ReturnNowPlaying {
    public TagPlaying(long discordId, String lastFMId, String tag, int playNumber) {
        super(discordId, lastFMId, tag, playNumber);
    }
}
