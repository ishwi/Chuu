package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

public final class LongExecutorChuuDatasource implements CommonDatasource {

    private static final String CONFIG = "/datasource.properties";
    private static final Logger log = LoggerFactory.getLogger(LongExecutorChuuDatasource.class);
    private final HikariDataSource ds;


    public LongExecutorChuuDatasource() {

        HikariConfig config = new HikariConfig(CONFIG);
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(5000);
        config.setValidationTimeout(250);
        config.setIdleTimeout(Duration.ofMinutes(2).toMillis());
        config.setThreadFactory(Thread.ofVirtual().uncaughtExceptionHandler((t, e) -> log.warn(e.getMessage(), e)).name("mariadb-pool-long", 0).factory());

        config.setMaxLifetime(Duration.ofMinutes(10).toMillis());
        config.setValidationTimeout(1000);
        config.setConnectionInitSql("set @@sql_mode='NO_ZERO_DATE';");
        config.setPoolName("Long-Pool-Chuu");
        config.addDataSourceProperty("connectionCollation", "utf8mb4_unicode_ci");
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
