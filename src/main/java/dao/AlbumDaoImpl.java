package dao;

import core.Chuu;
import core.exceptions.ChuuServiceException;
import core.exceptions.InstanceNotFoundException;
import dao.entities.*;
import org.intellij.lang.annotations.Language;

import java.sql.*;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AlbumDaoImpl implements AlbumDao {
    private String prepareINQuery(int size) {
        return String.join(",", Collections.nCopies(size, "(?,?)"));
    }


    @Override
    public void fillIds(Connection connection, List<ScrobbledAlbum> list) {
        String queryString = "SELECT id, album_name FROM  album WHERE  (artist_id,album_name)  IN (%s)  ";

        String sql = String.format(queryString, list.isEmpty() ? null : prepareINQuery(list.size()));

        UUID a = UUID.randomUUID();
        String seed = a.toString();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setLong(2 * i + 1, list.get(i).getArtistId());
                preparedStatement.setString(2 * i + 2, list.get(i).getAlbum());
            }

            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, ScrobbledAlbum> collect = list.stream().collect(Collectors.toMap(scrobbledAlbum -> scrobbledAlbum.getAlbumId() + "_" + seed + "_" + scrobbledAlbum.getAlbum(), Function.identity(), (scrobbledArtist, scrobbledArtist2) -> {
                scrobbledArtist.setCount(scrobbledArtist.getCount() + scrobbledArtist2.getCount());
                return scrobbledArtist;
            }));
            Pattern compile = Pattern.compile("\\p{M}");

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("album_name");
                ScrobbledAlbum scrobbledAlbum = collect.get(id + "_" + seed + "_" + name);
                if (scrobbledAlbum != null) {
                    scrobbledAlbum.setArtistId(id);
                } else {
                    // name can be stripped or maybe the element is collect is the stripped one
                    String normalizeArtistName = compile.matcher(
                            Normalizer.normalize(name, Normalizer.Form.NFKD)
                    ).replaceAll("");
                    ScrobbledAlbum normalizedArtist = collect.get(normalizeArtistName);
                    if (normalizedArtist != null) {
                        normalizedArtist.setArtistId(id);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteAllUserAlbums(Connection con, String lastfmId) {
        @Language("MariaDB") String queryString = "DELETE   FROM scrobbled_album  WHERE lastfm_id = ? ";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i, lastfmId);

            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void insertAlbums(Connection connection, List<ScrobbledAlbum> nonExistingId) {
        StringBuilder mySql =
                new StringBuilder("INSERT INTO album (artist_id,album_name,url,mbid) VALUES (?,?,?,?)");

        mySql.append(",(?,?,?,?)".repeat(Math.max(0, nonExistingId.size() - 1)));
        mySql.append(" on duplicate key update " +
                " mbid = if(mbid is null and values(mbid) is not null,values(mbid),mbid), " +
                " url = if(url is null and values(url) is not null,values(url),url)  returning id ");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            for (int i = 0; i < nonExistingId.size(); i++) {
                ScrobbledAlbum x = nonExistingId.get(i);
                preparedStatement.setLong(4 * i + 1, x.getArtistId());
                preparedStatement.setString(4 * i + 2, x.getAlbum());
                preparedStatement.setString(4 * i + 3, x.getUrl());
                String albumMbid = x.getAlbumMbid();
                if (albumMbid == null || albumMbid.isBlank()) {
                    albumMbid = null;
                }
                preparedStatement.setString(4 * i + 4, albumMbid);

            }
            preparedStatement.execute();

            ResultSet ids = preparedStatement.getResultSet();
            int counter = 0;
            while (ids.next()) {
                long aLong = ids.getLong(1);
                nonExistingId.get(counter++).setAlbumId(aLong);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertLastFmAlbum(Connection connection, ScrobbledAlbum x) {

        String sql = "INSERT INTO album (artist_id,album_name,url,mbid) VALUES (?,?,?,?) on duplicate key update" +
                " mbid = if(mbid is null and values(mbid) is not null,values(mbid),mbid), " +
                " url = if(url is null and values(url) is not null,values(url),url) " +

                "";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(+1, x.getArtistId());
            preparedStatement.setString(2, x.getAlbum());
            preparedStatement.setString(3, x.getUrl());
            String albumMbid = x.getAlbumMbid();
            if (albumMbid == null || albumMbid.isBlank()) {
                albumMbid = null;
            }
            preparedStatement.setString(4, albumMbid);

            preparedStatement.execute();

            ResultSet ids = preparedStatement.getResultSet();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                x.setAlbumId(generatedKeys.getLong("GENERATED_KEY"));
            } else {
                try {
                    long albumId = getAlbumIdByName(connection, x.getAlbum(), x.getArtistId());
                    x.setAlbumId(albumId);
                } catch (InstanceNotFoundException e) {
                    Chuu.getLogger().warn("ERROR CREATING {} {} {}", x, x.getAlbum(), x.getArtist());
                    //throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public long getAlbumIdByName(Connection connection, String album, long artist_id) throws InstanceNotFoundException {
        @Language("MariaDB") String queryString = "SELECT id FROM  album WHERE album_name = ? and artist_id = ?  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(2, artist_id);
            preparedStatement.setString(1, album);


            ResultSet execute = preparedStatement.executeQuery();
            if (execute.next()) {
                return execute.getLong(1);
            }
            throw new InstanceNotFoundException(album);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Album getAlbumByName(Connection connection, String album, long artist_id) throws InstanceNotFoundException {
        @Language("MariaDB") String queryString = "SELECT id,artist_id,album_name,url,rym_id,mbid,spotify_id FROM  album WHERE album_name = ? and artist_id = ?  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(2, artist_id);
            preparedStatement.setString(1, album);


            ResultSet execute = preparedStatement.executeQuery();
            if (execute.next()) {
                long id = execute.getLong(1);
                long artist_id_ = execute.getLong(2);
                String albumName = execute.getString(3);
                String url = execute.getString(4);
                long rym_id = execute.getLong(5);
                String mbid = execute.getString(6);
                String spotify_id = execute.getString(7);


                return new Album(id, artist_id, albumName, url, rym_id, UUID.fromString(mbid), spotify_id);
            }
            throw new InstanceNotFoundException(album);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void addSrobbledAlbums(Connection con, List<ScrobbledAlbum> scrobbledAlbums) {
        /* Create "queryString". */

        StringBuilder mySql =
                new StringBuilder("INSERT INTO  scrobbled_album" +
                        "                  (artist_id,album_id,lastfm_id,playnumber) VALUES (?,?,?,?) ");

        mySql.append(", (?,?,?,?)".repeat(Math.max(0, scrobbledAlbums.size() - 1)));
        mySql.append(" ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber");

        try {
            PreparedStatement preparedStatement = con.prepareStatement(mySql.toString());
            for (int i = 0; i < scrobbledAlbums.size(); i++) {
                ScrobbledAlbum scrobbledAlbum = scrobbledAlbums.get(i);
                preparedStatement.setLong(4 * i + 1, scrobbledAlbum.getArtistId());
                preparedStatement.setLong(4 * i + 2, scrobbledAlbum.getAlbumId());
                preparedStatement.setString(4 * i + 3, scrobbledAlbum.getDiscordID());
                preparedStatement.setInt(4 * i + 4, scrobbledAlbum.getCount());

            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<AlbumUserPlays> getUserTopArtistAlbums(Connection connection, long discord_id, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "select a.playnumber,b.album_name,d.name,b.url from scrobbled_album a join album b on a.album_id = b.id join user c on a.lastfm_id = c.lastfm_id " +
                "join artist d on b.artist_id = d.id  where c.discord_id = ? and a.artist_id = ?  order by a.playnumber desc  limit ? ";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setLong(i++, discord_id);
            preparedStatement.setLong(i++, artistId);
            processArtistAlbums(limit, returnList, preparedStatement, i);


        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;

    }

    @Override
    public List<AlbumUserPlays> getServerTopArtistAlbums(Connection connection, long guildId, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "select sum(a.playnumber) as ord,b.album_name,d.name,b.url from scrobbled_album a join album b on a.album_id = b.id join user c on a.lastfm_id = c.lastfm_id" +
                " join user_guild e on c.discord_id = e.discord_id " +
                "join artist d on b.artist_id = d.id  where e.guild_id = ? and a.artist_id = ?  " +
                " group by a.album_id " +
                "order by ord desc  limit ? ";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setLong(i++, guildId);
            preparedStatement.setLong(i++, artistId);
            processArtistAlbums(limit, returnList, preparedStatement, i);


        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;


    }

    @Override
    public List<AlbumUserPlays> getGlobalTopArtistAlbums(Connection connection, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "select sum(a.playnumber) as ord,b.album_name,d.name,b.url from scrobbled_album a join album b on a.album_id = b.id" +
                " join artist d on b.artist_id = d.id  where  a.artist_id = ?" +
                " group by a.album_id " +
                "  order by ord desc  limit ? ";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setLong(i++, artistId);
            processArtistAlbums(limit, returnList, preparedStatement, i);
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;

    }

    private void processArtistAlbums(int limit, List<AlbumUserPlays> returnList, PreparedStatement preparedStatement, int i) throws SQLException {
        preparedStatement.setInt(i, limit);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            int plays = resultSet.getInt(1);
            String albumName = resultSet.getString(2);
            String artist = resultSet.getString(3);
            String url = resultSet.getString(4);


            AlbumUserPlays e = new AlbumUserPlays(albumName, url);
            e.setArtist(artist);
            e.setPlays(plays);
            returnList.add(e);
        }
    }


}
