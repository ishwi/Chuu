package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class ChuuDatasource implements CommonDatasource {


    private static final Logger log = LoggerFactory.getLogger(ChuuDatasource.class);

    private static final String CONFIG = "/datasource.properties";
    private final HikariDataSource ds;


    public ChuuDatasource() {

        HikariConfig config = new HikariConfig(CONFIG);
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setMaximumPoolSize(18);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setPoolName("Normal-pool-Chuu");
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setConnectionInitSql("set @@sql_mode='NO_ZERO_DATE';");
        config.addDataSourceProperty("connectionCollation", "utf8mb4_unicode_ci");
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
