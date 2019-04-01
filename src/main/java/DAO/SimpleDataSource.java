package DAO;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

class SimpleDataSource implements DataSource {
	private static final String URL_PARAMETER =
			"SimpleDataSource.url";
	private static final String USER_PARAMETER =
			"SimpleDataSource.user";
	private static final String PASSWORD_PARAMETER =
			"SimpleDataSource.password";
	private static String url;
	private static String user;
	private static String password;
	private final ComboPooledDataSource cpds;

	public SimpleDataSource() {
		this.cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass("com.mysql.cj.jdbc.Driver"); //loads the jdbc driver
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		cpds.setJdbcUrl("jdbc:mysql://localhost:3306/lastfm");
		cpds.setUser("root");
		cpds.setPassword("root");

// the settings below are optional -- c3p0 can work with defaults
		cpds.setMinPoolSize(5);
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(20);
	}


	@Override
	public Connection getConnection() throws SQLException {
		return cpds.getConnection();
	}

	@Override
	public Connection getConnection(String s, String s1) {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> aClass) {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> aClass) {
		return false;
	}

	@Override
	public PrintWriter getLogWriter() {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter printWriter) {

	}

	@Override
	public int getLoginTimeout() {
		return 0;
	}

	@Override
	public void setLoginTimeout(int i) {

	}

	@Override
	public Logger getParentLogger() {
		return null;
	}
}
