package dao;

public record BotStats(long userCount, long guildCount, long artistCount, long albumCount, long scrobbledCount,
                       long rymCount, double rymAvg, long recCount, long correctionCount,
                       long randomCount, long imageCount, long voteCount, long setCount, long apiCount) {

}
