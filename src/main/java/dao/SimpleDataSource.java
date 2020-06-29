package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class SimpleDataSource {


    private final HikariDataSource ds;

    public SimpleDataSource(String file) {
        HikariConfig config = new HikariConfig(file);
        this.ds = new HikariDataSource(config);
    }

    public SimpleDataSource(boolean selector) {

        String name = "/datasource.properties";
        if (!selector)
            name = "/mbiz.properties";

        HikariConfig config = new HikariConfig(name);
        if (selector) {
            config.setConnectionInitSql("set @@sql_mode='NO_ZERO_DATE';");
            config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
            config.setMaximumPoolSize(20);

        }
        this.ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


}
