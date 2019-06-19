package DAO;

import DAO.Entities.*;
import org.apache.commons.collections4.map.MultiValueMap;

import javax.management.InstanceNotFoundException;
import javax.sql.DataSource;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DaoImplementation {
	private final DataSource dataSource;
	private final SQLQueriesDao queriesDao;
	private final UpdaterDao updaterDao;
	private final UserGuildDao userGuildDao;

	public DaoImplementation() {

		this.dataSource = new SimpleDataSource();
		this.queriesDao = new SQLQueriesDaoImpl();
		this.userGuildDao = new UserGuildDaoImpl();
		this.updaterDao = new UpdaterDaoImpl();
	}

	public void updateUserTimeStamp(String lastFmName) {
		try (Connection connection = dataSource.getConnection()) {
			updaterDao.setUpdatedTime(connection, lastFmName, null);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public void updateUserLibrary(List<ArtistData> list, String id) {
		try (Connection connection = dataSource.getConnection()) {

			try {

				/* Prepare connection. */
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				/* Do work. */
				list.forEach(artistData -> {
					artistData.setDiscordID(id);
					updaterDao.addArtist(connection, artistData);
					updaterDao.addUrl(connection, artistData);
				});
				updaterDao.setUpdatedTime(connection, id, null);

				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void incrementalUpdate(TimestampWrapper<LinkedList<ArtistData>> list, String id) {
		try (Connection connection = dataSource.getConnection()) {

			try {

				/* Prepare connection. */
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				/* Do work. */

				list.getWrapped().forEach(artistData -> {
					artistData.setDiscordID(id);
					updaterDao.upsertArtist(connection, artistData);
					updaterDao.upsertUrl(connection, new ArtistInfo(artistData.getUrl(), artistData.getArtist()));
				});
				updaterDao.setUpdatedTime(connection, id, list.getTimestamp());


				connection.commit();


			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
//			} catch (InstanceNotFoundException e) {
//				throw new RuntimeException(e);
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	public void addGuildUser(long userID, long guildID) {
		try (Connection connection = dataSource.getConnection()) {
			userGuildDao.addGuild(connection, userID, guildID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public void updateUserLibrary(LastFMData data) {
		try (Connection connection = dataSource.getConnection()) {

			try {
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				userGuildDao.insertUserData(connection, data);
				userGuildDao.addGuild(connection, data.getShowID(), data.getGuildID());
				connection.commit();

			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	public void removeUserCompletely(Long discordID) {

		try (Connection connection = dataSource.getConnection()) {
			try {
				/* Prepare connection. */

				connection.setAutoCommit(false);
				/* Do work. */
				userGuildDao.removeUser(connection, discordID);
				/* Commit. */
				connection.commit();

			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void removeFromGuild(Long discordID, long guildID) {

		try (Connection connection = dataSource.getConnection()) {
			try {
				/* Prepare connection. */

				connection.setAutoCommit(false);
				/* Do work. */
				userGuildDao.removeUserGuild(connection, discordID, guildID);
				/* Commit. */
				connection.commit();

			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	public void updateLastFmData(long discordID, String lastFMID) {

		try (Connection connection = dataSource.getConnection()) {

			try {

				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				LastFMData shows = userGuildDao.findLastFmData(connection, discordID);

				shows.setName(lastFMID);

				userGuildDao.updateLastFmData(connection, shows);
				connection.commit();

			} catch (SQLException e) {
				connection.rollback();
				throw new RuntimeException(e);
			} catch (RuntimeException | Error e) {
				connection.rollback();
				throw e;
			} catch (InstanceNotFoundException e) {
				e.printStackTrace();
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public LastFMData findLastFMData(long discordID) throws InstanceNotFoundException {
		try (Connection connection = dataSource.getConnection()) {
			return userGuildDao.findLastFmData(connection, discordID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public ResultWrapper getSimilarities(List<String> lastFmNames) throws InstanceNotFoundException {
		try (Connection connection = dataSource.getConnection()) {
			return queriesDao.similar(connection, lastFmNames);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public WrapperReturnNowPlaying whoKnows(String artist, long guildId) {
		return whoKnows(artist, guildId, 10);
	}

	public WrapperReturnNowPlaying whoKnows(String artist, long guildId, int limit) {
		try (Connection connection = dataSource.getConnection()) {
			return queriesDao.knows(connection, artist, guildId, limit);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public UsersWrapper getLessUpdated() {
		try (Connection connection = dataSource.getConnection()) {
			return updaterDao.getLessUpdated(connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<UsersWrapper> getAll(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			return userGuildDao.getAll(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public UniqueWrapper<UniqueData> getUniqueArtist(Long guildID, String lastFmId) {
		try (Connection connection = dataSource.getConnection()) {
			return queriesDao.getUniqueArtist(connection, guildID, lastFmId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Long> getGuildList(long userId) {
		try (Connection connection = dataSource.getConnection()) {
			return userGuildDao.guildList(connection, userId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<UrlCapsule> getGuildTop(long guildID) {
		try (Connection connection = dataSource.getConnection()) {
			return queriesDao.getGuildTop(connection, guildID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public UniqueWrapper<UniqueData> getCrowns(String lastFmID, long guildID) {
		try (Connection connection = dataSource.getConnection()) {
			return queriesDao.getCrowns(connection, lastFmID, guildID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getNullUrls() {
		try (Connection connection = dataSource.getConnection()) {
			return updaterDao.selectNullUrls(connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getSpotifyNulledUrls() {
		try (Connection connection = dataSource.getConnection()) {
			return updaterDao.selectNullUrls(connection, true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	public String getArtistUrl(String url) {
		try (Connection connection = dataSource.getConnection()) {
			return updaterDao.getArtistUrl(connection, url);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public void upsertUrl(ArtistInfo artistInfo) {
		try (Connection connection = dataSource.getConnection()) {
			updaterDao.upsertUrl(connection, artistInfo);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public void upsertSpotify(ArtistInfo artistInfo) {
		try (Connection connection = dataSource.getConnection()) {
			updaterDao.upsertSpotify(connection, artistInfo);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public void addLogo(long guildId, BufferedImage in) {
		try (Connection connection = dataSource.getConnection()) {
			userGuildDao.addLogo(connection, guildId, in);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	@Deprecated
	public void removeLogo(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			userGuildDao.removeLogo(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public InputStream findLogo(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			return userGuildDao.findLogo(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public MultiValueMap<Long, Long> getMapGuildUsers() {
		try (Connection connection = dataSource.getConnection()) {
			return userGuildDao.getWholeUser_Guild(connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	@Deprecated
	public long getDiscordIdFromLastfm(String lasFmName, long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			return userGuildDao.getDiscordIdFromLastFm(connection, lasFmName, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public int getArtistPlays(String artist, String whom) {
		try (Connection connection = dataSource.getConnection()) {
			return queriesDao.userPlays(connection, artist, whom);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public List<CrownsLbEntry> getGuildCrownLb(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			return queriesDao.crownsLeaderboard(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public void removeUserFromOneGuildConsequent(long discordID, long guildID) {
		removeFromGuild(discordID, guildID);
		MultiValueMap<Long, Long> map = getMapGuildUsers();
		if (!map.containsValue(discordID)) {
			removeUserCompletely(discordID);
		}
	}


}
