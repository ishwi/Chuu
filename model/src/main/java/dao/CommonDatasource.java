package dao;

import java.sql.Connection;
import java.sql.SQLException;

public sealed interface CommonDatasource permits ChuuDatasource, LongExecutorChuuDatasource, MbizDatasource, MonitoringDatasource {
    Connection getConnection() throws SQLException;
}
