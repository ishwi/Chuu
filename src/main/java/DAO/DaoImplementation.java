package DAO;

import DAO.Entities.*;
import main.last.TimestampWrapper;
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
	private final SQLShowsDao dao;

	public DaoImplementation() {

		this.dataSource = new SimpleDataSource();
		this.dao = new Jdbc3CcSQLShowsDao();

	}

	public void updateUserTimeStamp(String lastFmName) {
		try (Connection connection = dataSource.getConnection()) {
			dao.setUpdatedTime(connection, lastFmName, null);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public void updateUserLibrary(LinkedList<ArtistData> list, String id) {
		try (Connection connection = dataSource.getConnection()) {

			try {

				/* Prepare connection. */
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				/* Do work. */
				list.forEach(artistData -> {
					artistData.setDiscordID(id);
					dao.addArtist(connection, artistData);
					//dao.addUrl(connection, artistData);
				});
				dao.setUpdatedTime(connection, id, null);


				/* Commit. */
				// Seguramente haya mejor solucion


				connection.commit();

				// Relacionar post con show ?

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

	public void incrementalUpdate(TimestampWrapper<LinkedList<ArtistData>> list, String id) {
		try (Connection connection = dataSource.getConnection()) {

			try {

				/* Prepare connection. */
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				/* Do work. */

				list.getWrapped().forEach(artistData -> {
					artistData.setDiscordID(id);
					dao.upsertArtist(connection, artistData);
					//dao.upsertUrl(connection, artistData);
				});
				dao.setUpdatedTime(connection, id, list.getTimestamp());


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
			dao.addGuild(connection, userID, guildID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public void updateUserLibrary(LastFMData data) {
		try (Connection connection = dataSource.getConnection()) {

			try {
				/* Prepare connection. */
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				/* Do work. */
				LastFMData createdShow = dao.create(connection, data);
				dao.addGuild(connection, data.getShowID(), data.getGuildID());

				/* Commit. */
				// Seguramente haya mejor solucion


				connection.commit();

				// Relacionar post con show ?

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


	public void remove(Long discordID) {

		try (Connection connection = dataSource.getConnection()) {
			try {
				/* Prepare connection. */

				connection.setAutoCommit(false);
				/* Do work. */
				dao.removeUser(connection, discordID);
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

	public void updateShow(long discorID, String lastFMID) {

		try (Connection connection = dataSource.getConnection()) {

			try {

				/* Prepare connection. */
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				/* Do work. */
				// Obtenemos el show, lo validamos, y si no salta excepcion actualizamos

				LastFMData shows = dao.find(connection, discorID);

				shows.setName(lastFMID);


				dao.update(connection, shows);
				/* Commit. */
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

	public LastFMData findShow(long showID) throws InstanceNotFoundException {
		try (Connection connection = dataSource.getConnection()) {
			return dao.find(connection, showID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public ResultWrapper getSimilarities(List<String> lastfMNames) throws InstanceNotFoundException {
		try (Connection connection = dataSource.getConnection()) {
			return dao.similar(connection, lastfMNames);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public WrapperReturnNowPlaying whoKnows(String artist, long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			return dao.knows(connection, artist, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public UsersWrapper getLessUpdated() {
		try (Connection connection = dataSource.getConnection()) {
			return dao.getLessUpdated(connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<UsersWrapper> getAll(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			return dao.getAll(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public UniqueWrapper<UniqueData> getUniqueArtist(Long guildID, String lastFmId) {
		try (Connection connection = dataSource.getConnection()) {
			return dao.getUniqueArtist(connection, guildID, lastFmId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Long> getGuildList(long userId) {
		try (Connection connection = dataSource.getConnection()) {
			return dao.guildList(connection, userId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<UrlCapsule> getGuildTop(long guildID) {
		try (Connection connection = dataSource.getConnection()) {
			return dao.getGuildTop(connection, guildID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<UniqueData> getCrowns(String lastFmID, long guildID) {
		try (Connection connection = dataSource.getConnection()) {
			return dao.getCrowns(connection, lastFmID, guildID);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getNullUrls() {
		try (Connection connection = dataSource.getConnection()) {
			return dao.selectNullUrls(connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public String getArtistUrl(String url) {
		try (Connection connection = dataSource.getConnection()) {
			return dao.getArtistUrl(connection, url);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

	public void upsertUrl(ArtistInfo artistInfo) {
		try (Connection connection = dataSource.getConnection()) {
			dao.upsertUrl(connection, artistInfo);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public void addLogo(long guildId, BufferedImage in) {
		try (Connection connection = dataSource.getConnection()) {
			dao.addLogo(connection, guildId, in);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public void removeLogo(long guildId) {
		try (Connection connection = dataSource.getConnection()) {
			dao.removeLogo(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public InputStream findLogo(long guildId) throws InstanceNotFoundException {
		try (Connection connection = dataSource.getConnection()) {
			return dao.findLogo(connection, guildId);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}

	}

	public MultiValueMap<Long, Long> getMapGuildUsers() {
		try (Connection connection = dataSource.getConnection()) {
			return dao.getWholeUser_Guild(connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		}
	}

}
