package dao;

import java.sql.Connection;

public interface OAuthDAO {

    void storeDiscordRefreshToken(Connection connection, String accessToken, long discordId, Integer expiresIn);
}
