package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

public final class UpdateDatasource implements CommonDatasource {

    private static final Logger log = LoggerFactory.getLogger(UpdateDatasource.class);
    private static final String CONFIG = "/datasource.properties";
    private final HikariDataSource ds;


    public UpdateDatasource() {

        HikariConfig config = new HikariConfig(CONFIG);
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(Duration.ofMinutes(2).toMillis());
        config.setMaxLifetime(Duration.ofMinutes(10).toMillis());
        config.setValidationTimeout(1000);
        config.setPoolName("Update-Pool-Chuu");
        config.addDataSourceProperty("connectionCollation", "utf8mb4_unicode_ci");
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
