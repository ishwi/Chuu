package dao.utils;

import dao.entities.ResultSetConsumer;
import dao.entities.TriConsumer;
import dao.exceptions.ChuuServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SQLUtils {

    public static <T> void doBatches(Connection connection, String baseQuery, List<T> items, TriConsumer<PreparedStatement, T, Integer> mapPage, int bindPerRow) {
        doBatches(connection, baseQuery, items, mapPage, bindPerRow, "");
    }

    public static <T> void doBatches(Connection connection, String baseQuery, List<T> items, TriConsumer<PreparedStatement, T, Integer> mapPage, int bindPerRow, String additionalQuery) {
        if (items.isEmpty()) {
            return;
        }
        int batchSize = 65_534 / (bindPerRow + 2);
        int batches = (int) Math.max(1, Math.ceil(items.size() / (double) batchSize));
        int parelilsm = Math.max(1, Math.min(batches, 4));
        ForkJoinPool customThreadPool = new ForkJoinPool(parelilsm);
        String templatePart = " (" + IntStream.range(0, bindPerRow).mapToObj(w -> "?").collect(Collectors.joining(",")) + ") ";
        try {
            customThreadPool.submit(() -> IntStream.range(0, batches).parallel().forEach(currentBatch -> {
                try {
                    int startingPoint = currentBatch * batchSize;
                    int limit = Math.min((currentBatch + 1) * batchSize, items.size());
                    int elements = limit - startingPoint;
                    String query = baseQuery + templatePart + (("," + templatePart).repeat(elements - 1)) + additionalQuery;
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    int page = 0;
                    for (int i = startingPoint; i < limit; i++) {
                        T t = items.get(i);
                        mapPage.accept(preparedStatement, t, page++);
                    }
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new ChuuServiceException(e);
                }
            })).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ChuuServiceException(e);
        }
    }


    public static <T> void doBatchesSelect(Connection connection, String baseQuery, List<T> items, TriConsumer<PreparedStatement, T, Integer> mapPage,
                                           ResultSetConsumer consumer,
                                           int bindPerRow, String additionalQuery) {
        if (items.isEmpty()) {
            return;
        }
        int batchSize = 65_534 / (bindPerRow + 2);
        int batches = (int) Math.max(1, Math.ceil(items.size() / (double) batchSize));
        int parelilsm = Math.max(1, Math.min(batches, 4));
        ForkJoinPool customThreadPool = new ForkJoinPool(parelilsm);
        String templatePart = " (" + IntStream.range(0, bindPerRow).mapToObj(w -> "?").collect(Collectors.joining(",")) + ") ";
        try {
            customThreadPool.submit(() -> IntStream.range(0, batches).parallel().forEach(currentBatch -> {
                try {
                    int startingPoint = currentBatch * batchSize;
                    int limit = Math.min((currentBatch + 1) * batchSize, items.size());
                    int elements = limit - startingPoint;
                    String query = baseQuery + templatePart + (("," + templatePart).repeat(elements - 1)) + additionalQuery;
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    int page = 0;
                    for (int i = startingPoint; i < limit; i++) {
                        T t = items.get(i);
                        mapPage.accept(preparedStatement, t, page++);
                    }
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        consumer.accept(resultSet);
                    }
                } catch (SQLException e) {
                    throw new ChuuServiceException(e);
                }
            })).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ChuuServiceException(e);
        }
    }
}
