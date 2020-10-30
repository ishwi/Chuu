package dao;

public class BotStats {
    private final long user_count;
    private final long guild_count;
    private final long artistCount;
    private final long album_count1;
    private final long scrobbled_count;
    private final long rym_count;
    private final double rym_avg;
    private final long recommendation_count;
    private final long correction_count;
    private final long random_count;
    private final long image_count;
    private final long vote_count;
    private final long set_count;
    private final long api_count;

    public BotStats(long user_count, long guild_count, long artistCount, long album_count1, long scrobbled_count, long rym_count, double rym_avg, long recommendation_count, long correction_count, long random_count, long image_count, long vote_count, long set_count, long api_count) {


        this.user_count = user_count;
        this.guild_count = guild_count;
        this.artistCount = artistCount;
        this.album_count1 = album_count1;
        this.scrobbled_count = scrobbled_count;
        this.rym_count = rym_count;
        this.rym_avg = rym_avg;
        this.recommendation_count = recommendation_count;
        this.correction_count = correction_count;
        this.random_count = random_count;
        this.image_count = image_count;
        this.vote_count = vote_count;
        this.set_count = set_count;
        this.api_count = api_count;
    }

    public long getUser_count() {
        return user_count;
    }

    public long getGuild_count() {
        return guild_count;
    }

    public long getArtistCount() {
        return artistCount;
    }

    public long getAlbum_count1() {
        return album_count1;
    }

    public long getScrobbled_count() {
        return scrobbled_count;
    }

    public long getRym_count() {
        return rym_count;
    }

    public double getRym_avg() {
        return rym_avg;
    }

    public long getRecommendation_count() {
        return recommendation_count;
    }

    public long getCorrection_count() {
        return correction_count;
    }

    public long getRandom_count() {
        return random_count;
    }

    public long getImage_count() {
        return image_count;
    }

    public long getVote_count() {
        return vote_count;
    }

    public long getSet_count() {
        return set_count;
    }

    public long getApi_count() {
        return api_count;
    }
}
