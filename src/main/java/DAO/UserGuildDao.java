package DAO;

import DAO.Entities.LastFMData;
import DAO.Entities.UsersWrapper;
import org.apache.commons.collections4.map.MultiValueMap;

import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;

interface UserGuildDao {

	void insertUserData(Connection con, LastFMData show);

	LastFMData findLastFmData(Connection con, Long showID) throws InstanceNotFoundException;

	List<Long> guildList(Connection connection, long userId);

	MultiValueMap<Long, Long> getWholeUser_Guild(Connection connection);

	void updateLastFmData(Connection con, LastFMData show);

	void removeUser(Connection con, Long showID);

	void removeUserGuild(Connection con, long discordId, long guildId);

	List<UsersWrapper> getAll(Connection connection, long guildId);

	void addGuild(Connection con, long userId, long guildId);

	void addLogo(Connection con, long guildID, BufferedImage image);

	void removeLogo(Connection connection, long guildId);

	InputStream findLogo(Connection connection, long guildID);

	long getDiscordIdFromLastFm(Connection connection, String lastFmName, long guildId);


}
