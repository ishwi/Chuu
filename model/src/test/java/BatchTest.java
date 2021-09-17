import dao.entities.TriConsumer;
import dao.utils.SQLUtils;
import org.apache.commons.collections4.Bag;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BatchTest {
    @Test
    public void zeroItems() {
        Assertions.assertDoesNotThrow(() ->
                SQLUtils.doBatches(null, null, Collections.emptyList(), null, 0, null));
    }

    @Test
    public void oneItem() throws SQLException {
        Connection mock = mock(Connection.class);
        String base = "INSERT INTO scrobbled_track(artist_id,track_id,lastfm_id,playnumber,loved) VALUES";
        PreparedStatement ps = mock(PreparedStatement.class);
        when(mock.prepareStatement(any())).thenReturn(ps);
        AtomicInteger settedCount = new AtomicInteger(0);

        when(ps.executeUpdate()).thenReturn(0);
        Bag<Integer> objects = new UniqueBag<>(Integer.class);

        List<String> strings = IntStream.range(0, 1).mapToObj(String::valueOf).toList();
        TriConsumer<PreparedStatement, String, Integer> mapPage = (preparedStatement, s, page) -> {
            objects.add(4 * page + 1);
            objects.add(4 * page + 2);
            objects.add(4 * page + 3);
            objects.add(4 * page + 4);
            ps.setString(4 * page + 1, s);
            ps.setString(4 * page + 2, s);
            ps.setString(4 * page + 3, s);
            ps.setString(4 * page + 4, s);
            settedCount.addAndGet(4);
        };
        int bindPerRow = 4;
        String additionalQuery = " ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber, loved = VALUES(loved) ";

        SQLUtils.doBatches(mock, base, strings, mapPage, bindPerRow, additionalQuery);

        Assertions.assertEquals(4, settedCount.get());
        for (int i = 1; i <= 4; i++) {
            Assertions.assertEquals(1, objects.getCount(i));
        }
    }

    @Test
    public void onePage() throws SQLException {
        Connection mock = mock(Connection.class);
        String base = "INSERT INTO scrobbled_track(artist_id,track_id,lastfm_id,playnumber,loved) VALUES";
        PreparedStatement ps = mock(PreparedStatement.class);
        when(mock.prepareStatement(any())).thenReturn(ps);
        AtomicInteger settedCount = new AtomicInteger(0);

        when(ps.executeUpdate()).thenReturn(0);
        Bag<Integer> objects = new UniqueBag<>(Integer.class);

        List<String> strings = IntStream.range(0, 100).mapToObj(String::valueOf).toList();
        TriConsumer<PreparedStatement, String, Integer> mapPage = (preparedStatement, s, page) -> {
            objects.add(4 * page + 1);
            objects.add(4 * page + 2);
            objects.add(4 * page + 3);
            objects.add(4 * page + 4);
            ps.setString(4 * page + 1, s);
            ps.setString(4 * page + 2, s);
            ps.setString(4 * page + 3, s);
            ps.setString(4 * page + 4, s);
            settedCount.addAndGet(4);
        };
        int bindPerRow = 4;
        String additionalQuery = " ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber, loved = VALUES(loved) ";

        SQLUtils.doBatches(mock, base, strings, mapPage, bindPerRow, additionalQuery);

        Assertions.assertEquals(100 * 4, settedCount.get());
        for (int i = 1; i <= 100; i++) {
            Assertions.assertEquals(1, objects.getCount(i));
        }
    }

    @Test
    public void testBulk() throws SQLException {
        Connection mock = mock(Connection.class);
        String base = "INSERT INTO scrobbled_track(artist_id,track_id,lastfm_id,playnumber,loved) VALUES";
        PreparedStatement ps = mock(PreparedStatement.class);
        when(mock.prepareStatement(any())).thenReturn(ps);
        AtomicInteger settedCount = new AtomicInteger(0);

        when(ps.executeUpdate()).thenReturn(0);
        Bag<Integer> objects = new UniqueBag<>(Integer.class);

        List<String> strings = IntStream.range(0, 10922).mapToObj(String::valueOf).toList();
        TriConsumer<PreparedStatement, String, Integer> mapPage = (preparedStatement, s, page) -> {
            objects.add(4 * page + 1);
            objects.add(4 * page + 2);
            objects.add(4 * page + 3);
            objects.add(4 * page + 4);
            ps.setString(4 * page + 1, s);
            ps.setString(4 * page + 2, s);
            ps.setString(4 * page + 3, s);
            ps.setString(4 * page + 4, s);
            settedCount.addAndGet(4);
        };
        int bindPerRow = 4;
        String additionalQuery = " ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber, loved = VALUES(loved) ";

        SQLUtils.doBatches(mock, base, strings, mapPage, bindPerRow, additionalQuery);

        Assertions.assertEquals(10922 * 4, settedCount.get());
        for (int i = 1; i <= 10922; i++) {
            Assertions.assertEquals(1, objects.getCount(i));
        }
    }

    @Test
    public void testBulkTwoPages() throws SQLException {
        Connection mock = mock(Connection.class);
        String base = "INSERT INTO scrobbled_track(artist_id,track_id,lastfm_id,playnumber,loved) VALUES";
        PreparedStatement ps = mock(PreparedStatement.class);
        when(mock.prepareStatement(any())).thenReturn(ps);
        AtomicInteger settedCount = new AtomicInteger(0);

        when(ps.executeUpdate()).thenReturn(0);
        Bag<Integer> objects = new UniqueBag<>(Integer.class);

        List<String> strings = IntStream.range(0, 10923).mapToObj(String::valueOf).toList();
        TriConsumer<PreparedStatement, String, Integer> mapPage = (preparedStatement, s, page) -> {
            objects.add(4 * page + 1);
            objects.add(4 * page + 2);
            objects.add(4 * page + 3);
            objects.add(4 * page + 4);
            ps.setString(4 * page + 1, s);
            ps.setString(4 * page + 2, s);
            ps.setString(4 * page + 3, s);
            ps.setString(4 * page + 4, s);
            settedCount.addAndGet(4);
        };
        int bindPerRow = 4;
        String additionalQuery = " ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber, loved = VALUES(loved) ";

        SQLUtils.doBatches(mock, base, strings, mapPage, bindPerRow, additionalQuery);

        Assertions.assertEquals(10923 * 4, settedCount.get());
        Assertions.assertEquals(2, objects.getCount(1));
        Assertions.assertEquals(2, objects.getCount(2));
        Assertions.assertEquals(2, objects.getCount(3));
        Assertions.assertEquals(2, objects.getCount(4));

        for (int i = 5; i <= 10923; i++) {
            Assertions.assertEquals(1, objects.getCount(i));
        }
    }

    @Test
    public void testBulkMultiple() throws SQLException {
        Connection mock = mock(Connection.class);
        String base = "INSERT INTO scrobbled_track(artist_id,track_id,lastfm_id,playnumber,loved) VALUES";
        PreparedStatement ps = mock(PreparedStatement.class);
        when(mock.prepareStatement(any())).thenReturn(ps);
        AtomicInteger settedCount = new AtomicInteger(0);

        when(ps.executeUpdate()).thenReturn(0);
        Bag<Integer> objects = new UniqueBag<>(Integer.class);

        List<String> strings = IntStream.range(0, 65538).mapToObj(String::valueOf).toList();
        TriConsumer<PreparedStatement, String, Integer> mapPage = (preparedStatement, s, page) -> {
            objects.add(4 * page + 1);
            objects.add(4 * page + 2);
            objects.add(4 * page + 3);
            objects.add(4 * page + 4);
            ps.setString(4 * page + 1, s);
            ps.setString(4 * page + 2, s);
            ps.setString(4 * page + 3, s);
            ps.setString(4 * page + 4, s);
            settedCount.addAndGet(4);
        };
        int bindPerRow = 4;
        String additionalQuery = " ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber, loved = VALUES(loved) ";

        SQLUtils.doBatches(mock, base, strings, mapPage, bindPerRow, additionalQuery);

        Assertions.assertEquals(65538 * 4, settedCount.get());
        for (int i = 1; i <= 24; i++) {
            Assertions.assertEquals(7, objects.getCount(i));
        }

        for (int i = 25; i <= 10922 * 4; i++) {
            Assertions.assertEquals(6, objects.getCount(i));
        }
    }

    @Test
    public void testAlbums() throws SQLException {
        Connection mock = mock(Connection.class);
        String base = "INSERT INTO scrobbled_track(artist_id,track_id,lastfm_id,playnumber,loved) VALUES";
        PreparedStatement ps = mock(PreparedStatement.class);
        when(mock.prepareStatement(any())).thenReturn(ps);
        AtomicInteger settedCount = new AtomicInteger(0);

        when(ps.executeUpdate()).thenReturn(0);
        Bag<Integer> objects = new UniqueBag<>(Integer.class);
        int SIZE = 19381;
        List<String> strings = IntStream.range(0, SIZE).mapToObj(String::valueOf).toList();
        TriConsumer<PreparedStatement, String, Integer> mapPage = (preparedStatement, s, page) -> {
            objects.add(4 * page + 1);
            objects.add(4 * page + 2);
            objects.add(4 * page + 3);
            objects.add(4 * page + 4);
            ps.setString(4 * page + 1, s);
            ps.setString(4 * page + 2, s);
            ps.setString(4 * page + 3, s);
            ps.setString(4 * page + 4, s);
            settedCount.addAndGet(4);
        };
        int bindPerRow = 4;
        String additionalQuery = " ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber, loved = VALUES(loved) ";

        SQLUtils.doBatches(mock, base, strings, mapPage, bindPerRow, additionalQuery);

        Assertions.assertEquals(SIZE * 4, settedCount.get());
        int batchSize = 10922;
        int batches = 2;
        for (int i = 1; i <= (19381 - 10922) * 4; i++) {
            Assertions.assertEquals(2, objects.getCount(i));
        }

        for (int i = ((19381 - 10922) * 4) + 1; i <= 10922 * 4; i++) {
            Assertions.assertEquals(1, objects.getCount(i));
        }
    }
}
