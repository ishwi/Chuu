package dao.everynoise;

import dao.entities.NoiseGenre;
import dao.exceptions.ChuuServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class EveryNoiseDAOImpl implements EveryNoiseDAO {

    @Override
    public List<NoiseGenre> fuzzyMatch(Connection connection, String genre) {
        String sql = "SELECT genre,uri FROM every_noise_genre a " +
                     " ORDER BY levenhestein(?,genre) ASC  LIMIT 20";
        List<NoiseGenre> genres = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, genre);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String matched = resultSet.getString(1);
                String uri = resultSet.getString(3);
                genres.add(new NoiseGenre(matched, uri));
            }
            return genres;
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
    }

    @Override
    public List<Release> releasesOfGenre(Connection connection, LocalDate week, NoiseGenre noiseGenre) {
        String sql = "SELECT artist,`release`,uri FROM every_noise_release a JOIN every_noise_release_genre b ON a.release_id = b.release_id JOIN every_noise_genre c ON c.genre_id = c.id" +
                     " WHERE `week`  =  ? AND c.genre = ?";
        List<Release> genres = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, week);
            preparedStatement.setString(2, noiseGenre.name());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String artist = resultSet.getString(1);
                String release = resultSet.getString(2);
                String uri = resultSet.getString(3);
                genres.add(new Release(artist, release, uri));
            }
            return genres;
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
    }

    @Override
    public List<NoiseGenre> listAll(Connection connection) {
        String sql = "SELECT genre,playlist FROM every_noise_genre";
        List<NoiseGenre> genres = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                genres.add(new NoiseGenre(resultSet.getString(1), resultSet.getString(2)));
            }
            return genres;
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
    }

    @Override
    public void insertGenres(Connection connection, List<NoiseGenre> genres) {
        String sql = "INSERT INTO every_noise_genre(genre,playlist) VALUES %s";
        String value = genres.stream().map(z -> "(?,?)").collect(Collectors.joining(","));
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql.formatted(value))) {
            int setter = 1;
            for (NoiseGenre genre : genres) {
                preparedStatement.setString(setter++, genre.name());
                preparedStatement.setString(setter++, genre.uri());
            }
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
    }

    @Override
    public Map<Release, Long> insertReleases(Connection connection, List<Release> release, LocalDate week) {
        String sql = "INSERT ignore INTO every_noise_release(week,artist,`release`,uri) VALUES %s returning id,artist,`release`,uri ";
        String value = release.stream().map(z -> "(?,?,?,?)").collect(Collectors.joining(","));
        Map<Release, Long> idMap = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql.formatted(value))) {
            int setter = 1;
            for (Release genre : release) {
                preparedStatement.setObject(setter++, week);
                preparedStatement.setString(setter++, genre.artist());
                preparedStatement.setString(setter++, genre.release());
                preparedStatement.setString(setter++, genre.uri());
            }
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getResultSet();

            while (resultSet.next()) {
                resultSet.getLong(1);
                resultSet.getString(3);
                resultSet.getString(4);
                idMap.put(new Release(resultSet.getString(2), resultSet.getString(3), resultSet.getString(4)), resultSet.getLong(1));
            }
            return idMap;
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
    }


    @Override
    public void insertGenreRelases(Connection connection, Map<Long, Set<String>> idToGenres) {
        String subSelect = """
                WITH
                    temp(id, genre) AS (VALUES %s)
                SELECT b.id,temp.id
                FROM
                    temp
                        JOIN
                        every_noise_genre b ON b.genre = temp.genre""";

        String valuesExpr = idToGenres.entrySet().stream().flatMap(z ->
                z.getValue().stream().map(genre -> "(" + z.getKey() + ",?)")
        ).collect(Collectors.joining(","));
        String values = subSelect.formatted(valuesExpr);
        String sql = "INSERT INTO every_noise_release_genre(genre_id,release_id) " + values;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql.formatted(sql))) {
            int setter = 1;

            for (Set<String> strings : idToGenres.values()) {
                for (String str : strings) {
                    preparedStatement.setString(setter++, str);
                }
            }
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
    }
}
