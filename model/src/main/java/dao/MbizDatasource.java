package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class MbizDatasource implements CommonDatasource {

    private static final String CONFIG = "/mbiz.properties";
    private final HikariDataSource ds;


    public MbizDatasource(boolean selector) {
        HikariConfig config = new HikariConfig(CONFIG);
        config.setThreadFactory(Thread.ofVirtual().name("musicbrinz", 0).factory());
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
