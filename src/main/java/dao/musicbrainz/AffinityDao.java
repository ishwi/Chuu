package dao.musicbrainz;

import dao.entities.Affinity;
import dao.entities.LbEntry;

import java.sql.Connection;
import java.util.List;

public interface AffinityDao {
    void initTempTable(Connection connection, String ownerLastfmID, String receiverLastFMId, int threshold);

    Affinity getPercentageStats(Connection connection, String ownerLastfmID, String receiverLastFMId, int threshold);

    String[] doRecommendations(Connection connection, String ogLastFmId, String receiverLastFMId);


    void setServerTempTable(Connection connection, long guildId, String ogLastfmID, int threshold);

    List<LbEntry> getMatchingCount(Connection connection);

    List<Affinity> doServerAffinity(Connection connection, String ogLastfmId, int threshold);


    void cleanUp(Connection connection, boolean isServer);
}
