package dao;

import dao.entities.ObscurityStats;

public record ServerStats(ObscurityStats stats, long memberCount, long commmandCount, String topCommand,
                          long countOnTopCommand,
                          long randomCount,
                          long voteCount, long imageCount, long recCount) {

}
