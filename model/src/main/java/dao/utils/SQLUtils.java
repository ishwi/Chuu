package dao.utils;

import dao.entities.ResultSetConsumer;
import dao.entities.TriConsumer;
import dao.exceptions.ChuuServiceException;
import jdk.incubator.concurrent.StructuredTaskScope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
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
        String templatePart = " (" + IntStream.range(0, bindPerRow).mapToObj(w -> "?").collect(Collectors.joining(",")) + ") ";
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int index = 0; index < batches; index++) {
                int batch = index;
                scope.fork(() -> {
                    int startingPoint = batch * batchSize;
                    int limit = Math.min((batch + 1) * batchSize, items.size());
                    int elements = limit - startingPoint;
                    String query = baseQuery + templatePart + (("," + templatePart).repeat(elements - 1)) + additionalQuery;
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    int page = 0;
                    for (int i = startingPoint; i < limit; i++) {
                        T t = items.get(i);
                        mapPage.accept(preparedStatement, t, page++);
                    }
                    preparedStatement.executeUpdate();
                    return 0;
                });
            }
            scope.join();
            scope.throwIfFailed(ChuuServiceException::new);
        } catch (InterruptedException e) {
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
        String templatePart = " (" + IntStream.range(0, bindPerRow).mapToObj(w -> "?").collect(Collectors.joining(",")) + ") ";

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int index = 0; index < batches; index++) {
                int batch = index;
                scope.fork(() -> {
                    int startingPoint = batch * batchSize;
                    int limit = Math.min((batch + 1) * batchSize, items.size());
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
                    return 0;
                });
            }
            scope.join();
            scope.throwIfFailed(ChuuServiceException::new);
        } catch (InterruptedException e) {
            throw new ChuuServiceException(e);
        }

    }
}
