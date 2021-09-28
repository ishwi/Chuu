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
        config.setConnectionInitSql("set @@sql_mode='NO_ZERO_DATE';");
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setMaximumPoolSize(14);
        config.setMinimumIdle(8);
        config.setPoolName("Normal-pool-Chuu");
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
