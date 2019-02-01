package main;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class SimpleDataSource implements DataSource {
    private static final String URL_PARAMETER =
            "SimpleDataSource.url";
    private static final String USER_PARAMETER =
            "SimpleDataSource.user";
    private static final String PASSWORD_PARAMETER =
            "SimpleDataSource.password";
    private static String url;
    private static String user;
    private static String password;

    private synchronized void readConfiguration() {
        if (url == null) {
            try {
                /* Read configuration parameters. */
                url = "jdbc:mysql://localhost:3306/lastfm";
                user = "root";
                password = "root";

                try (Connection connection = DriverManager.getConnection(url, user, password)) {
                    System.out.println("Database connected!");
                } catch (SQLException e) {
                    throw new IllegalStateException("Cannot connect the database!", e);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public Connection getConnection() throws SQLException {
        readConfiguration();
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public Connection getConnection(String s, String s1) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int i) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
