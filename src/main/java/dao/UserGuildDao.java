package dao;

import core.exceptions.InstanceNotFoundException;
import dao.entities.LastFMData;
import dao.entities.UsersWrapper;
import org.apache.commons.collections4.MultiValuedMap;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;

interface UserGuildDao {

    void createGuild(Connection con, long guildId);

    void insertUserData(Connection con, LastFMData lastFMData);

    LastFMData findLastFmData(Connection con, long discordId) throws core.exceptions.InstanceNotFoundException;

    List<Long> guildsFromUser(Connection connection, long userId);

    MultiValuedMap<Long, Long> getWholeUserGuild(Connection connection);

    void updateLastFmData(Connection con, LastFMData lastFMData);

    void removeUser(Connection con, Long discordId);

    void removeUserGuild(Connection con, long discordId, long guildId);

    List<UsersWrapper> getAll(Connection connection, long guildId);

    void addGuild(Connection con, long userId, long guildId);

    void addLogo(Connection con, long guildID, BufferedImage image);

    void removeLogo(Connection connection, long guildId);

    InputStream findLogo(Connection connection, long guildID);

    long getDiscordIdFromLastFm(Connection connection, String lastFmName, long guildId) throws InstanceNotFoundException;


    LastFMData findByLastFMId(Connection connection, String lastFmID) throws InstanceNotFoundException;


    List<UsersWrapper> getAll(Connection connection);

    void removeRateLimit(Connection connection, long discordId);

    void upsertRateLimit(Connection connection, long discordId, float queriesPerSecond);

}
