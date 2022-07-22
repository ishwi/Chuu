import dao.entities.TriConsumer;
import dao.utils.SQLUtils;
import org.apache.commons.collections4.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
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

        assertThat(settedCount.get()).isEqualTo(4);
        for (int i = 1; i <= 4; i++) {
            assertThat(objects.getCount(i)).isEqualTo(1);
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

        assertThat(settedCount.get()).isEqualTo(100 * 4);
        for (int i = 1; i <= 100; i++) {
            assertThat(objects.getCount(i)).isEqualTo(1);
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

        assertThat(settedCount.get()).isEqualTo(10922 * 4);
        for (int i = 1; i <= 10922; i++) {
            assertThat(objects.getCount(i)).isEqualTo(1);
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

        assertThat(settedCount.get()).isEqualTo(10923 * 4);
        assertThat(objects.getCount(1)).isEqualTo(2);
        assertThat(objects.getCount(2)).isEqualTo(2);
        assertThat(objects.getCount(3)).isEqualTo(2);
        assertThat(objects.getCount(4)).isEqualTo(2);

        for (int i = 5; i <= 10923; i++) {
            assertThat(objects.getCount(i)).isEqualTo(1);
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

        assertThat(settedCount.get()).isEqualTo(65538 * 4);
        for (int i = 1; i <= 24; i++) {
            assertThat(objects.getCount(i)).isEqualTo(7);
        }

        for (int i = 25; i <= 10922 * 4; i++) {
            assertThat(objects.getCount(i)).isEqualTo(6);
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

        assertThat(settedCount.get()).isEqualTo(SIZE * 4);
        int batchSize = 10922;
        int batches = 2;
        for (int i = 1; i <= (19381 - 10922) * 4; i++) {
            assertThat(objects.getCount(i)).isEqualTo(2);
        }

        for (int i = ((19381 - 10922) * 4) + 1; i <= 10922 * 4; i++) {
            assertThat(objects.getCount(i)).isEqualTo(1);
        }
    }
}
