package dao;

import dao.entities.AlbumInfo;
import dao.entities.ArtistInfo;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import dao.exceptions.ChuuServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DiscoveralDaoImpl implements DiscoveralDao {
    @Override
    public void setDiscoveredAlbumTempTable(Connection connection, Collection<ScrobbledAlbum> scrobbledAlbums, String lastfmId) {

        String queryBody =
                """
                        CREATE TEMPORARY TABLE discovered_temp(
                        artist_name varchar(400) COLLATE  utf8mb4_unicode_ci ,
                        album_name varchar(400) COLLATE  utf8mb4_unicode_ci ,
                        album_id bigint PRIMARY KEY ,
                        play_count int
                        ) DEFAULT CHARSET=utf8mb4 COLLATE =  utf8mb4_general_ci;""".indent(11);

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();
            if (scrobbledAlbums.isEmpty()) {
                return;
            }
            queryBody =
                    "INSERT INTO discovered_temp(artist_name,album_name,album_id,play_count)  VALUES %s";
            String sql = String.format(queryBody, String.join(",", Collections.nCopies(scrobbledAlbums.size(), "(?,?,?,?)")));
            try (PreparedStatement preparedStatement2 = connection.prepareStatement(sql)) {

                int i = 1;
                for (ScrobbledAlbum scrobbledAlbum : scrobbledAlbums) {
                    preparedStatement2.setString(i++, scrobbledAlbum.getArtist());
                    preparedStatement2.setString(i++, scrobbledAlbum.getAlbum());
                    preparedStatement2.setLong(i++, scrobbledAlbum.getAlbumId());
                    preparedStatement2.setInt(i++, scrobbledAlbum.getCount());
                }
                preparedStatement2.execute();

            } catch (SQLException e) {
                throw new ChuuServiceException(e);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void setDiscoveredArtistTempTable(Connection connection, Collection<ScrobbledArtist> scrobbledAlbums, String lastfmId) {

        String queryBody =
                """
                        CREATE TEMPORARY TABLE discovered_artist_temp(
                        id BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                        artist_name varchar(400) COLLATE  utf8mb4_unicode_ci,
                        play_count int
                        ) DEFAULT CHARSET=utf8mb4 COLLATE =  utf8mb4_general_ci;"""
                        .indent(11);

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();
            if (scrobbledAlbums.isEmpty()) {
                return;
            }

            queryBody =
                    "                                INSERT INTO discovered_artist_temp(artist_name,play_count)  VALUES %s";
            String sql = String.format(queryBody, String.join(",", Collections.nCopies(scrobbledAlbums.size(), "(?,?)")));
            try (PreparedStatement preparedStatement2 = connection.prepareStatement(sql)) {

                int i = 1;
                for (ScrobbledArtist scrobbledAlbum : scrobbledAlbums) {
                    preparedStatement2.setString(i++, scrobbledAlbum.getArtist());

                    preparedStatement2.setInt(i++, scrobbledAlbum.getCount());
                }
                preparedStatement2.execute();

            } catch (SQLException e) {
                throw new ChuuServiceException(e);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public List<AlbumInfo> calculateDiscoveryFromAlbumTemp(Connection connection, String lastfmId) {
        List<AlbumInfo> returnedMap = new ArrayList<>();
        String s = """
                SELECT b.album_name,c.name,a.play_count FROM discovered_temp a LEFT JOIN album b
                ON a.album_id = b.id
                LEFT JOIN artist c ON b.artist_id =  c.id LEFT JOIN scrobbled_album d ON b.id = d.album_id\s
                WHERE a.play_count >= coalesce(d.playnumber,0) AND d.lastfm_id = ?
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setString(1, lastfmId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String album = resultSet.getString(1);
                String artist = resultSet.getString(2);

                returnedMap.add(new AlbumInfo(album, artist));

            }
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
        return returnedMap;
    }

    @Override
    public Set<ArtistInfo> calculateDiscoveryFromArtistTemp(Connection connection, String lastfmId) {
        Set<ArtistInfo> returnedMap = new HashSet<>();
        String s = """
                SELECT c.name,c.url
                FROM discovered_artist_temp a
                LEFT JOIN artist c ON a.artist_name = c.name
                LEFT JOIN scrobbled_artist d ON c.id = d.artist_id\s
                WHERE a.play_count >= coalesce(d.playnumber,0) AND d.lastfm_id = ?
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setString(1, lastfmId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String url = resultSet.getString(2);
                String artist = resultSet.getString(1);


                returnedMap.add(new ArtistInfo(url, artist));

            }
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
        return returnedMap;
    }

    @Override
    public void deleteDiscoveryAlbumTempTable(Connection connection) {
        String queryBody = "DROP TABLE IF EXISTS  discovered_temp";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    @Override
    public void deleteDiscoveryArtistTable(Connection connection) {
        String queryBody = "DROP TABLE IF EXISTS  discovered_artist_temp";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }
}
