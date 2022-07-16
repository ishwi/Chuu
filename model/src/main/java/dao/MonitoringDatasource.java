package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class MonitoringDatasource implements CommonDatasource {

    private static final Logger log = LoggerFactory.getLogger(MonitoringDatasource.class);
    private static final String CONFIG = "/datasource.properties";
    private final HikariDataSource ds;


    public MonitoringDatasource() {

        HikariConfig config = new HikariConfig(CONFIG);
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setMaximumPoolSize(4);
        config.setMinimumIdle(4);
        config.setIdleTimeout(0);
        config.setConnectionTimeout(250);
        config.setAutoCommit(false);
        config.setReadOnly(true);
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setPoolName("Monitoring-Pool-Chuu");
        config.addDataSourceProperty("connectionCollation", "utf8mb4_unicode_ci");
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
