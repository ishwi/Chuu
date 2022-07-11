package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ChuuDatasource implements CommonDatasource {

    private static final String CONFIG = "/datasource.properties";
    private final HikariDataSource ds;


    public ChuuDatasource() {

        HikariConfig config = new HikariConfig(CONFIG);
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setMaximumPoolSize(12);
        config.setMinimumIdle(2);
        config.setThreadFactory(Thread.ofVirtual().name("Short-Chuu-", 0).factory());
        config.setPoolName("Normal-pool-Chuu");
        config.addDataSourceProperty("connectionCollation", "utf8mb4_unicode_ci");
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
