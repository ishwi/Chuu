package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class MbizDatasource implements CommonDatasource {
    private static final Logger log = LoggerFactory.getLogger(MbizDatasource.class);

    private static final String CONFIG = "/mbiz.properties";
    private final HikariDataSource ds;


    public MbizDatasource(boolean selector) {
        HikariConfig config = new HikariConfig(CONFIG);
        config.setThreadFactory(Thread.ofVirtual().uncaughtExceptionHandler((t, e) -> log.warn(e.getMessage(), e)).name("musicbrinz", 0).factory());
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
