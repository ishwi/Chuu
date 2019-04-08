package DAO;

import DAO.Entities.*;

import javax.management.InstanceNotFoundException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class DaoImplementation {
	private final DataSource dataSource;
	private final SQLShowsDao dao;

	public DaoImplementation() {

		this.dataSource = new SimpleDataSource();
		this.dao = new Jdbc3CcSQLShowsDao();

	}

	public void addData(LinkedList<ArtistData> list, String id) {
		try (Connection connection = dataSource.getConnection()) {

			try {

				/* Prepare connection. */
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);

				/* Do work. */
				list.forEach(artistData -> {
					artistData.setDiscordID(id);
					dao.addArtist(connection, artistData);
					dao.addUrl(connection, artistData);
				});
				dao.setUpdatedTime(connection, id);


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

	public void addData(LastFMData data) {
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
				connection.setTransactionIsolation(
						Connection.TRANSACTION_SERIALIZABLE);
				connection.setAutoCommit(false);
				/* Do work. */
				dao.remove(connection, discordID);
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

	public UniqueWrapper<UniqueData> getUniqueArtist(Long guildID, String  lastFmId) {
		try (Connection connection = dataSource.getConnection()) {
			return dao.getUniqueArtist(connection, guildID, lastFmId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}
}
