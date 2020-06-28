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

    List<BillboardEntity> getArtistBillboard(Connection connection, int week_id, long idLong, boolean doListeners);

    List<BillboardEntity> getGlobalArtistBillboard(Connection connection, int week_id, boolean doListeners);

    List<BillboardEntity> getAlbumBillboard(Connection connection, int week_id, long idLong, boolean doListeners);

    List<BillboardEntity> getGlobalAlbumBillboard(Connection connection, int week_id, boolean doListeners);

    void insertBillboardDataScrobbles(Connection con, int week_id, long guildId);

    void insertBillboardDataListeners(Connection connection, int week_id, long guildId);


    List<PreBillboardUserData> getUserData(Connection connection, String lastfmId, int weekId);

    void insertUserData(Connection connection, List<TrackWithArtistId> trackList, String lastfmId, int weekId);

    void insertBillboardDataScrobblesByArtist(Connection connection, int week_id, long guildId);

    void insertBillboardDataListenersByArtist(Connection connection, int week_id, long guildId);

    void insertBillboardDataListenersByAlbum(Connection connection, int week_id, long guildId);

    void insertBillboardDataScrobblesByAlbum(Connection connection, int week_id, long guildId);

    List<BillboardEntity> getGlobalBillboard(Connection connection, int weekId, boolean doListeners);

    void insertGlobalBillboardDataScrobblesByAlbum(Connection connection, int week_id);

    void insertGlobalBillboardDataListenersByAlbum(Connection connection, int week_id);

    void insertGlobalBillboardDataScrobblesByArtist(Connection connection, int week_id);

    void insertGlobalBillboardDataListenersByArtist(Connection connection, int week_id);

    void insertGlobalBillboardDataScrobbles(Connection connection, int week_id);

    void insertGlobalBillboardDataListeners(Connection connection, int week_id);
}
