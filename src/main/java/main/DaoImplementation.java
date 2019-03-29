package main;

import DAO.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DaoImplementation {
    private DataSource dataSource;
    private SQLShowsDao dao;

    public DaoImplementation() {
        this.dataSource = new SimpleDataSource();
        this.dao = new Jdbc3CcSQLShowsDao();

    }

    public void addData(Map<String, Integer> map, String id) {
        try (Connection connection = dataSource.getConnection()) {

            try {

                /* Prepare connection. */
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                connection.setAutoCommit(false);

                /* Do work. */
                map.forEach((k, v) -> dao.addArtist(connection, new ArtistData(id, k, v)));


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

    public LastFMData addData(LastFMData data)  {
        try (Connection connection = dataSource.getConnection()) {

            try {

                /* Prepare connection. */
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                connection.setAutoCommit(false);

                /* Do work. */
                LastFMData createdShow = dao.create(connection, data);

                /* Commit. */
                // Seguramente haya mejor solucion


                connection.commit();

                // Relacionar post con show ?
                return createdShow;

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
    public void updateShow(long discorID, String lastFMID)
            {

        try (Connection connection = dataSource.getConnection()) {

            try {

                /* Prepare connection. */
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                connection.setAutoCommit(false);

                /* Do work. */
                // Obtenemos el show, lo validamos, y si no salta excepcion actualizamos

                LastFMData shows = dao.find( connection,discorID);

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
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public LastFMData findShow(long showID)  {
        try (Connection connection = dataSource.getConnection()) {
            return dao.find(connection, showID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
