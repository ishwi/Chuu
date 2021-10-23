package dao;

import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.SQLUtils;

import java.sql.*;
import java.text.Normalizer;
import java.time.Year;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AlbumDaoImpl extends BaseDAO implements AlbumDao {

    private String prepareINQuerySingle(int size) {
        return String.join(",", Collections.nCopies(size, "(?)"));
    }

    private String prepareINQueryTuple(int size) {
        return String.join(",", Collections.nCopies(size, "(?,?)"));
    }


    @Override
    public void fillIds(Connection connection, List<ScrobbledAlbum> list) {
        String queryString = "SELECT id,artist_id,album_name FROM  album USE INDEX (artist_id) WHERE  (artist_id,album_name) in  (%s)  ";

        String sql = String.format(queryString, list.isEmpty() ? null : prepareINQueryTuple(list.size()));

        UUID a = UUID.randomUUID();
        String seed = a.toString();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setLong(2 * i + 1, list.get(i).getArtistId());
                preparedStatement.setString(2 * i + 2, list.get(i).getAlbum());
            }

            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, ScrobbledAlbum> albumMap = list.stream().collect(Collectors.toMap(scrobbledAlbum -> scrobbledAlbum.getArtistId() + "_" + seed + "_" + scrobbledAlbum.getAlbum().toLowerCase(), Function.identity(), (scrobbledArtist, scrobbledArtist2) -> {
                scrobbledArtist.setCount(scrobbledArtist.getCount() + scrobbledArtist2.getCount());
                return scrobbledArtist;
            }));
            Pattern compile = Pattern.compile("\\p{M}");

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                long artist_id = resultSet.getLong("artist_id");

                String name = resultSet.getString("album_name");
                ScrobbledAlbum scrobbledAlbum = albumMap.get(artist_id + "_" + seed + "_" + name.toLowerCase());
                if (scrobbledAlbum != null) {
                    scrobbledAlbum.setAlbumId(id);
                } else {
                    // name can be stripped or maybe the element is collect is the stripped one
                    String normalizeArtistName = compile.matcher(
                            Normalizer.normalize(name, Normalizer.Form.NFKD)
                    ).replaceAll("");
                    ScrobbledAlbum normalizedArtist = albumMap.get(artist_id + "_" + seed + "_" + normalizeArtistName.toLowerCase());
                    if (normalizedArtist != null) {
                        normalizedArtist.setAlbumId(id);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteAllUserAlbums(Connection con, String lastfmId) {
        String queryString = "DELETE   FROM scrobbled_album  WHERE lastfm_id = ? ";
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

        String sql = "INSERT INTO album (artist_id,album_name,url,mbid) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE" +
                " mbid = if(mbid IS NULL AND values(mbid) IS NOT NULL,values(mbid),mbid), " +
                " url = if(url IS NULL AND values(url) IS NOT NULL,values(url),url) RETURNING id" +

                "";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(+1, x.getArtistId());
            String album = x.getAlbum();
            preparedStatement.setString(2, album);
            preparedStatement.setString(3, x.getUrl());
            String albumMbid = x.getAlbumMbid();
            if (albumMbid == null || albumMbid.isBlank()) {
                albumMbid = null;
            }
            preparedStatement.setString(4, albumMbid);

            preparedStatement.execute();

            ResultSet ids = preparedStatement.getResultSet();
            if (ids.next()) {
                x.setAlbumId(ids.getLong(1));
            } else {
                try {
                    if (album.length() > 400) {
                        album = album.substring(0, 400);
                    }
                    long albumId = getAlbumIdByName(connection, album, x.getArtistId());
                    x.setAlbumId(albumId);
                } catch (InstanceNotFoundException e) {
                    logger.warn("ERROR CREATING {} {} {}", x, album, x.getArtist());
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
        String queryString = "SELECT id FROM  album WHERE album_name = ? AND artist_id = ?  ";
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
    public String getAlbumUrlByName(Connection connection, String album, long artist_id) throws InstanceNotFoundException {
        String queryString = "SELECT url FROM  album WHERE album_name = ? AND artist_id = ?  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(2, artist_id);
            preparedStatement.setString(1, album);


            ResultSet execute = preparedStatement.executeQuery();
            if (execute.next()) {
                return execute.getString(1);
            }
            throw new InstanceNotFoundException(album);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<AlbumInfo> get(Connection connection, List<AlbumInfo> albumInfos, Year year) {
        Map<AlbumInfo, AlbumInfo> identiyMap = albumInfos.stream().collect(Collectors.toMap(x -> new AlbumInfo(x.getName(), x.getArtist()), x -> x, (x, y) -> x));
        List<AlbumInfo> found = new ArrayList<>();
        String queryString = "SELECT album_name,name  FROM album a join artist b on a.artist_id = b.id  WHERE (album_name,name) in (%s) and release_year = ? ";
        String sql = String.format(queryString, albumInfos.isEmpty() ? null : prepareINQueryTuple(albumInfos.size()), albumInfos.isEmpty() ? null : prepareINQueryTuple(albumInfos.size()));


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < albumInfos.size(); i++) {
                preparedStatement.setString(2 * i + 1, albumInfos.get(i).getName());
                preparedStatement.setString(2 * i + 2, albumInfos.get(i).getArtist());
            }
            preparedStatement.setInt(albumInfos.size() * 2 + 1, year.getValue());


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String tag = resultSet.getString("album_name");
                String artistName = resultSet.getString("name");
                AlbumInfo albumInfo = new AlbumInfo(tag, artistName);
                AlbumInfo t = identiyMap.get(albumInfo);
                if (t != null) {
                    found.add(t);
                }
            }
            return found;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public List<ScrobbledAlbum> getUserAlbumsOfYear(Connection connection, String username, Year year) {
        List<ScrobbledAlbum> scrobbledAlbums = new ArrayList<>();
        String s = "SELECT b.album_name,c.name,b.url,b.mbid,a.playnumber  FROM scrobbled_album a JOIN album b ON a.album_id = b.id JOIN artist c ON a.artist_id = c.id  WHERE a.lastfm_id = ? AND b.release_year = ? ORDER BY a.playnumber DESC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, year.getValue());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String album = resultSet.getString(1);
                String artist = resultSet.getString(2);
                String url = resultSet.getString(3);
                String mbid = resultSet.getString(4);
                int playnumber = resultSet.getInt(5);
                ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(album, artist, url, mbid);
                scrobbledAlbum.setCount(playnumber);
                scrobbledAlbums.add(scrobbledAlbum);
            }
        } catch (
                SQLException throwables) {

            throw new ChuuServiceException(throwables);
        }
        return scrobbledAlbums;
    }

    @Override
    public List<ScrobbledAlbum> getUserAlbumsWithNoYear(Connection connection, String username, int limit) {
        List<ScrobbledAlbum> scrobbledAlbums = new ArrayList<>();
        String s = "SELECT b.album_name,c.name,b.url,b.mbid,a.playnumber  FROM scrobbled_album a JOIN album b ON a.album_id = b.id JOIN artist c ON a.artist_id = c.id  WHERE a.lastfm_id = ? AND b.release_year IS NULL ORDER BY a.playnumber DESC LIMIT ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, limit);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String album = resultSet.getString(1);
                String artist = resultSet.getString(2);
                String url = resultSet.getString(3);
                String mbid = resultSet.getString(4);
                int playnumber = resultSet.getInt(5);
                ScrobbledAlbum scrobbledAlbum = new ScrobbledAlbum(album, artist, url, mbid);
                scrobbledAlbum.setCount(playnumber);
                scrobbledAlbums.add(scrobbledAlbum);
            }
        } catch (
                SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
        return scrobbledAlbums;
    }

    @Override
    public void insertAlbumOfYear(Connection connection, AlbumInfo albumInfos, Year year) {
        String sql = "UPDATE album JOIN artist  ON album.artist_id = artist.id SET album.release_year = ?  WHERE (album.album_name = ?  AND artist.name = ?)   ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, year.getValue());
            preparedStatement.setString(2, albumInfos.getName());
            preparedStatement.setString(3, albumInfos.getArtist());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void insertAlbumsOfYear(Connection connection, List<AlbumInfo> albumInfos, Year year) {
        String queryString = "update album join artist  on album.artist_id = artist.id set album.release_year = ?  WHERE (album.album_name,artist.name) in (%s) and release_year is null ";
        String sql = String.format(queryString, albumInfos.isEmpty() ? null : prepareINQueryTuple(albumInfos.size()), albumInfos.isEmpty() ? null : prepareINQueryTuple(albumInfos.size()));


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, year.getValue());
            for (int i = 0; i < albumInfos.size(); i++) {
                preparedStatement.setString(2 * i + 2, albumInfos.get(i).getName());
                preparedStatement.setString(2 * i + 3, albumInfos.get(i).getArtist());
            }


            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }


    @Override
    public Album getAlbumByName(Connection connection, String album, long artist_id) throws InstanceNotFoundException {
        String queryString = "SELECT id,artist_id,album_name,url,rym_id,mbid,spotify_id FROM  album WHERE album_name = ? AND artist_id = ?  ";
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


                return new Album(id, artist_id, albumName, url, rym_id, mbid == null ? null : UUID.fromString(mbid), spotify_id);
            }
            throw new InstanceNotFoundException(album);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public void addSrobbledAlbums(Connection con, List<ScrobbledAlbum> scrobbledAlbums) {

        SQLUtils.doBatches(con, "INSERT INTO scrobbled_album(artist_id,album_id,lastfm_id,playnumber) VALUES ", scrobbledAlbums, (ps, sc, page) -> {
            ps.setLong(4 * page + 1, sc.getArtistId());
            ps.setLong(4 * page + 2, sc.getAlbumId());
            ps.setString(4 * page + 3, sc.getDiscordID());
            ps.setInt(4 * page + 4, sc.getCount());
        }, 4, " ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber");


//        int batches = scrobbledAlbums.size() / 8000;
//        int j = 0;
//        for (int x = 0; x < batches; x++) {
//
//            StringBuilder mySql =
//                    new StringBuilder("INSERT INTO  scrobbled_album" +
//                                      "                  (artist_id,album_id,lastfm_id,playnumber) VALUES (?,?,?,?) ");
//            int startingPoint = x * batches;
//            x
//            mySql.append(", (?,?,?,?)".repeat(Math.max(0, Math.min(8000 - 1, scrobbledAlbums.size() - 1))));
//            mySql.append(" ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber");
//            try {
//                PreparedStatement preparedStatement = con.prepareStatement(mySql.toString());
//                for (int i = 0; i < 8000; i++) {
//                    ScrobbledAlbum scrobbledAlbum = scrobbledAlbums.get(j++);
//                    preparedStatement.setLong(4 * i + 1, scrobbledAlbum.getArtistId());
//                    preparedStatement.setLong(4 * i + 2, scrobbledAlbum.getAlbumId());
//                    preparedStatement.setString(4 * i + 3, scrobbledAlbum.getDiscordID());
//                    preparedStatement.setInt(4 * i + 4, scrobbledAlbum.getCount());
//                }
//                preparedStatement.execute();
//            } catch (SQLException e) {
//                throw new ChuuServiceException(e);
//            }
//        }
    }

    @Override
    public List<AlbumUserPlays> getUserTopArtistAlbums(Connection connection, long discord_id, long artistId,
                                                       int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "SELECT a.playnumber,b.album_name,d.name,b.url FROM scrobbled_album a JOIN album b ON a.album_id = b.id JOIN user c ON a.lastfm_id = c.lastfm_id " +
                "JOIN artist d ON b.artist_id = d.id  WHERE c.discord_id = ? AND a.artist_id = ?  ORDER BY a.playnumber DESC  LIMIT ? ";
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
    public List<AlbumUserPlays> getServerTopArtistAlbums(Connection connection, long guildId, long artistId,
                                                         int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "SELECT sum(a.playnumber) AS ord,b.album_name,d.name,b.url FROM scrobbled_album a JOIN album b ON a.album_id = b.id JOIN user c ON a.lastfm_id = c.lastfm_id" +
                " JOIN user_guild e ON c.discord_id = e.discord_id " +
                "JOIN artist d ON b.artist_id = d.id  WHERE e.guild_id = ? AND a.artist_id = ?  " +
                " GROUP BY a.album_id " +
                "ORDER BY ord DESC  LIMIT ? ";
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

    private void processArtistAlbums(int limit, List<AlbumUserPlays> returnList, PreparedStatement
            preparedStatement, int i) throws SQLException {
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

    @Override
    public List<AlbumUserPlays> getGlobalTopArtistAlbums(Connection connection, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "SELECT sum(a.playnumber) AS ord,b.album_name,d.name,b.url FROM scrobbled_album a JOIN album b ON a.album_id = b.id" +
                " JOIN artist d ON b.artist_id = d.id  WHERE  a.artist_id = ?" +
                " GROUP BY a.album_id " +
                "  ORDER BY ord DESC  LIMIT ? ";
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


    @Override
    public Map<Genre, Integer> genreCountsByAlbum(Connection connection, List<AlbumInfo> albumInfos) {

        String queryString = "SELECT tag,count(*) as coun FROM album a join artist b on a.artist_id =b.id join album_tags c  on a.id = c.album_id WHERE (name) in (%s) and (album_name)  IN (%s) group by tag";
        String sql = String.format(queryString, albumInfos.isEmpty() ? null : prepareINQuerySingle(albumInfos.size()), albumInfos.isEmpty() ? null : prepareINQuerySingle(albumInfos.size()));


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < albumInfos.size(); i++) {
                preparedStatement.setString(i + 1, albumInfos.get(i).getArtist());
                preparedStatement.setString(i + 1 + albumInfos.size(), albumInfos.get(i).getName());
            }

            /* Fill "preparedStatement". */


            Map<Genre, Integer> returnList = new HashMap<>();
            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String tag = resultSet.getString("tag");
                int count = resultSet.getInt("coun");

                returnList.put(new Genre(tag), count);

            }
            return returnList;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public Map<Year, Integer> countByYears(Connection connection, String lastfmId, List<AlbumInfo> albumInfos) {

        String queryString = "SELECT" +
                " release_year,count(*) as coun FROM scrobbled_album c join  album a on c.album_id = a.id  join artist b on a.artist_id =b.id  WHERE (name) in (%s) and (album_name)  IN (%s) and release_year is not null  and lastfm_id = ? group by release_year";

        String sql = String.format(queryString, albumInfos.isEmpty() ? null : prepareINQuerySingle(albumInfos.size()), albumInfos.isEmpty() ? null : prepareINQuerySingle(albumInfos.size()));


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < albumInfos.size(); i++) {
                preparedStatement.setString(i + 1, albumInfos.get(i).getArtist());
                preparedStatement.setString(i + 1 + albumInfos.size(), albumInfos.get(i).getName());
            }
            preparedStatement.setString(albumInfos.size() * 2 + 1, lastfmId);


            Map<Year, Integer> years = new HashMap<>();
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                int year = resultSet.getInt(1);
                int count = resultSet.getInt(2);

                years.put(Year.of(year), count);
            }
            return years;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Map<Year, Integer> countByDecades(Connection connection, String lastfmId, List<AlbumInfo> albumInfos) {

        String queryString = "SELECT" +
                " floor(release_year/10) * 10,count(*) as coun FROM scrobbled_album c join  album a on c.album_id = a.id  join artist b on a.artist_id =b.id  WHERE (name) in (%s) and (album_name)  IN (%s) and release_year is not null  and lastfm_id = ? group by floor(release_year/10)*10 ";

        String sql = String.format(queryString, albumInfos.isEmpty() ? null : prepareINQuerySingle(albumInfos.size()), albumInfos.isEmpty() ? null : prepareINQuerySingle(albumInfos.size()));


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < albumInfos.size(); i++) {
                preparedStatement.setString(i + 1, albumInfos.get(i).getArtist());
                preparedStatement.setString(i + 1 + albumInfos.size(), albumInfos.get(i).getName());
            }
            preparedStatement.setString(albumInfos.size() * 2 + 1, lastfmId);


            Map<Year, Integer> years = new HashMap<>();
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                int year = resultSet.getInt(1);
                int count = resultSet.getInt(2);

                years.put(Year.of(year), count);
            }
            return years;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Set<String> getAlbumTags(Connection connection, long albumId) {
        String queryString = "SELECT tag FROM album_tags WHERE album_id = ? ";
        Set<String> returnList = new HashSet<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, albumId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("tag");
                returnList.add(name);
            }
            return returnList;

        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }

    }


}
