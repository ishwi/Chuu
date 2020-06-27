package dao;

import core.commands.BillboardEntity;
import dao.entities.PreBillboardUserData;
import dao.entities.TrackWithArtistId;
import dao.entities.Week;

import java.sql.Connection;
import java.util.List;

public interface BillboardDao {

    Week getCurrentWeekId(Connection connection);

    List<BillboardEntity> getBillboard(Connection connection, int week_id, long idLong, boolean doListeners);

    void insertBillboardDataScrobbles(Connection con, int week_id, long guildId);

    void insertBillboardDataListeners(Connection connection, int week_id, long guildId);


    List<PreBillboardUserData> getUserData(Connection connection, String lastfmId, int weekId);

    void insertUserData(Connection connection, List<TrackWithArtistId> trackList, String lastfmId, int weekId);
}
