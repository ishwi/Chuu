package dao;

import dao.exceptions.ChuuServiceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;

public class OAuthDAOImpl implements OAuthDAO {

    @Override
    public void storeDiscordRefreshToken(Connection connection, String accessToken, long discordId, Integer expiresIn) {
        String sql = "UPDATE user SET discord_access_token = ?, discord_access_token_expires = ? WHERE discord_id = ?";
        try (var ps = connection.prepareStatement(sql)) {
            ps.setString(1, accessToken);
            ps.setLong(2, discordId);
            ps.setObject(3, Instant.ofEpochSecond(expiresIn));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }
}
