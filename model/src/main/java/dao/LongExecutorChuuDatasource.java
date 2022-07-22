package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class LongExecutorChuuDatasource implements CommonDatasource {

    private static final Logger log = LoggerFactory.getLogger(LongExecutorChuuDatasource.class);
    private static final String CONFIG = "/datasource.properties";
    private final HikariDataSource ds;


    public LongExecutorChuuDatasource() {

        HikariConfig config = new HikariConfig(CONFIG);
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(15000);
        config.setConnectionInitSql("set @@sql_mode='NO_ZERO_DATE';");
        config.setPoolName("Long-Pool-Chuu");
        config.addDataSourceProperty("connectionCollation", "utf8mb4_unicode_ci");
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
