package dao;

import dao.entities.Affinity;
import dao.entities.ArtistLbGlobalEntry;
import dao.entities.GlobalAffinity;
import dao.entities.LbEntry;

import java.sql.Connection;
import java.util.List;

public interface AffinityDao {
    void initTempTable(Connection connection, String ownerLastfmID, String receiverLastFMId, long threshold);

    Affinity getPercentageStats(Connection connection, String ownerLastfmID, String receiverLastFMId, long threshold);

    String[] doRecommendations(Connection connection, String ogLastFmId, String receiverLastFMId);


    void setServerTempTable(Connection connection, long guildId, String ogLastfmID, long threshold);

    void setGlobalTable(Connection connection, String ogLastfmID, long threshold);

    List<ArtistLbGlobalEntry> getGlobalMatchingCount(Connection connection);

    List<LbEntry> getMatchingCount(Connection connection);

    List<GlobalAffinity> doGlobalAffinity(Connection connection, String ogLastfmId, long threshold);

    List<Affinity> doServerAffinity(Connection connection, String ogLastfmId, long threshold);


    void cleanUp(Connection connection, boolean isServer);

    void cleanUpGlobal(Connection connection, boolean isServer);

}
