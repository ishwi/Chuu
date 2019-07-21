package DAO;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class SimpleDataSource implements DataSource {
	private static final String URL_PARAMETER =
			"SimpleDataSource.url";
	private static final String USER_PARAMETER =
			"SimpleDataSource.user";
	private static final String PASSWORD_PARAMETER =
			"SimpleDataSource.password";
	private static final String DRIVER_CLASS =
			"SimpleDataSource.driverClass";
	private static String url;
	private static String user;
	private static String password;
	private final ComboPooledDataSource cpds;


	public SimpleDataSource(String file) {
		Properties properties = new Properties();
		try (InputStream in = SimpleDataSource.class.getResourceAsStream(file)) {
			properties.load(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(properties.getProperty(DRIVER_CLASS)); //loads the jdbc driver
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		cpds.setJdbcUrl(properties.getProperty(URL_PARAMETER));
		cpds.setUser(properties.getProperty(USER_PARAMETER));
		cpds.setPassword(properties.getProperty(PASSWORD_PARAMETER));

		cpds.setMinPoolSize(5);
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(25);
	}


	public SimpleDataSource(boolean selector) {

		Properties properties = new Properties();
		String name = "/datasource.properties";
		if (!selector)
			name = "/mbiz.properties";
		try (InputStream in = SimpleDataSource.class.getResourceAsStream(name)) {
			properties.load(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(properties.getProperty(DRIVER_CLASS)); //loads the jdbc driver
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		cpds.setJdbcUrl(properties.getProperty(URL_PARAMETER));
		cpds.setUser(properties.getProperty(USER_PARAMETER));
		cpds.setPassword(properties.getProperty(PASSWORD_PARAMETER));

// the settings below are optional -- c3p0 can work with defaults
		cpds.setMinPoolSize(5);
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(25);
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
	public PrintWriter getLogWriter() {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter printWriter) {

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
	public Logger getParentLogger() {
		return null;
	}

	@Override
	public int getLoginTimeout() {
		return 0;
	}

	@Override
	public void setLoginTimeout(int i) {

	}


}
