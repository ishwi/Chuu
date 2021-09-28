package dao;

import java.sql.Connection;
import java.sql.SQLException;

public sealed interface CommonDatasource permits ChuuDatasource, LongExecutorChuuDatasource, MbizDatasource {
    Connection getConnection() throws SQLException;
}
