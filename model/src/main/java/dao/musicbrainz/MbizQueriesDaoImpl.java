package dao.musicbrainz;

import com.neovisionaries.i18n.CountryCode;
import dao.BaseDAO;
import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;


public class MbizQueriesDaoImpl extends BaseDAO implements MbizQueriesDao {


    @Override
    public List<CountWrapper<AlbumInfo>> getYearAverage(Connection con, List<AlbumInfo> albumInfos, Year year) {
        List<CountWrapper<AlbumInfo>> returnList = new ArrayList<>();

        StringBuilder queryString = new StringBuilder("""
                SELECT\s
                a.name as albumname,a.gid as mbid,b.name artistName,(sum(e.length) / count(*)) as av\s
                FROM
                    musicbrainz.release a
                     join musicbrainz.artist_credit b ON a.artist_credit = b.id
                       JOIN
                    musicbrainz.release_group c ON a.release_group = c.id
                        join musicbrainz.medium f on f.release = a.id\s
                \t\tjoin musicbrainz.track e on f.id = e.medium
                \t\tJOIN
                    musicbrainz.release_group_meta d ON c.id = d.id
                \t
                 Where d.first_release_date_year = ? and     a.gid in (""");
        for (AlbumInfo ignored : albumInfos) {
            queryString.append(" ? ,");
        }
        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + " ) group by a.gid,a.name,b.name");

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString.toString())) {
            int i = 1;
            preparedStatement.setInt(i++, year.get(ChronoField.YEAR));

            for (AlbumInfo albumInfo : albumInfos) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String mbid = resultSet.getString("mbid");
                String artist = resultSet.getString("artistName");
                String albumName = resultSet.getString("albumname");
                int average = resultSet.getInt("av");

                AlbumInfo ai = new AlbumInfo(mbid, albumName, artist);
                returnList.add(new CountWrapper<>(average, ai));
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }


    @Override
    public List<AlbumInfo> getYearAlbums(Connection con, List<AlbumInfo> albumInfos, Year year) {
        List<AlbumInfo> returnList = new ArrayList<>();

        StringBuilder queryString = new StringBuilder("""
                SELECT\s
                a.name as albumname,a.gid as mbid,b.name artistName
                FROM
                    musicbrainz.release a
                     join musicbrainz.artist_credit b ON a.artist_credit = b.id
                       JOIN
                    musicbrainz.release_group c ON a.release_group = c.id
                        JOIN
                    musicbrainz.release_group_meta d ON c.id = d.id Where d.first_release_date_year = ? and     a.gid in (""");
        for (AlbumInfo ignored : albumInfos) {
            queryString.append(" ? ,");
        }
        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString.toString())) {
            int i = 1;
            preparedStatement.setInt(i++, year.get(ChronoField.YEAR));

            for (AlbumInfo albumInfo : albumInfos) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String mbid = resultSet.getString("mbid");
                String artist = resultSet.getString("artistName");
                String albumName = resultSet.getString("albumname");
                AlbumInfo ai = new AlbumInfo(mbid, albumName, artist);
                returnList.add(ai);
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<AlbumInfo> getYearAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, Year year) {
        String queryString = """
                SELECT DISTINCT
                    (a.name) as artistname, b.name as albumname,  d.first_release_date_year as year\s
                FROM
                    musicbrainz.artist_credit a
                        JOIN
                    musicbrainz.release b ON a.id = b.artist_credit
                        JOIN
                    musicbrainz.release_group c ON b.release_group = c.id
                        JOIN
                    musicbrainz.release_group_meta d ON c.id = d.id\s""";
        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.substring(0, albumWhere.length() - 1) + ") ";
        whereSentence += "and d.first_release_date_year = ?";

        List<AlbumInfo> returnList = new ArrayList<>();
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {

                preparedStatement.setString(i, albumInfo.getArtist());
                preparedStatement.setString(i + releaseInfo.size(), albumInfo.getName());
                i++;
            }

            prepareRealeaseYearStatement(releaseInfo, year, returnList, preparedStatement);
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<AlbumInfo> getDecadeAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, int decade, int numberOfYears) {
        String queryString = """
                SELECT DISTINCT
                    (a.name) as artistname, b.name as albumname,  d.first_release_date_year as year\s
                FROM
                    musicbrainz.artist_credit a
                        JOIN
                    musicbrainz.release b ON a.id = b.artist_credit
                        JOIN
                    musicbrainz.release_group c ON b.release_group = c.id
                        JOIN
                    musicbrainz.release_group_meta d ON c.id = d.id\s""";
        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.substring(0, albumWhere.length() - 1) + ") ";
        whereSentence += "and (d.first_release_date_year between ? and ?)";

        List<AlbumInfo> returnList = new ArrayList<>();
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {

                preparedStatement.setString(i, albumInfo.getName());
                preparedStatement.setString(i + releaseInfo.size(), albumInfo.getName());
                i++;
            }

            prepareRealeaseDecadeStatement(releaseInfo, decade, returnList, preparedStatement, numberOfYears);
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }


    @Override
    public MultiValuedMap<Genre, String> genreCount(Connection con, List<AlbumInfo> releaseInfo) {
        MultiValuedMap<Genre, String> returnMap = new HashSetValuedHashMap<>();
        List<Genre> list = new ArrayList<>();
        StringBuilder queryString = new StringBuilder("""
                SELECT\s
                       c.name as neim, d.gid as mbid
                \s
                 FROM
                 musicbrainz.release d join\s
                 musicbrainz.release_group a  on d.release_group = a.id         JOIN
                    musicbrainz.release_group_tag b ON a.id = b.release_group
                        JOIN
                    musicbrainz.tag c ON b.tag = c.id
                WHERE
                    d.gid in (""");

        for (AlbumInfo ignored : releaseInfo) {
            queryString.append(" ? ,");
        }

        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString.toString())) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String genreName = resultSet.getString("neim");
                String mbid = resultSet.getString("mbid");
                Genre genre = new Genre(genreName);
                returnMap.put(genre, mbid);
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnMap;
    }

    @Override
    public Map<Country, Integer> countryCount(Connection connection, List<ArtistInfo> releaseInfo) {
        Map<Country, Integer> returnMap = new HashMap<>();


        StringBuilder queryString = new StringBuilder("""
                select main.code,count(*) as count from (
                SELECT\s
                      (case\s
                \t  when b.type = 1 then coalesce(c.code,d.country,'NOTVALID')
                \t  else coalesce(d.country,calculate_country(b.id),'NOTVALID') end) as code\s
                \s
                 FROM
                 musicbrainz.artist a left join\s
                 musicbrainz.area b on a.area = b.id    left join musicbrainz.iso_3166_1 c  on b.id=c.area and b.type = 1 left join country_lookup d on b.id = d.id and b.type != 1   WHERE     a.gid in (""");

        for (ArtistInfo ignored : releaseInfo) {
            queryString.append(" ? ,");
        }

        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");
        queryString.append(") main   GROUP BY main.code\n");

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;

            for (ArtistInfo albumInfo : releaseInfo) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                String code = resultSet.getString("code");
                if (code == null || code.equals("NOTVALID")) {
                    continue;
                }
                int frequency = resultSet.getInt("count");

                returnMap.put(new Country(code, code), frequency);
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnMap;

    }

    @Override
    public List<Track> getAlbumTrackList(Connection connection, String artist, String album) {
        List<Track> returnList = new ArrayList<>();

        String queryString = """
                SELECT distinct e.name , e.position
                FROM\s
                musicbrainz.artist_credit a
                JOIN
                musicbrainz.release b ON a.id = b.artist_credit
                JOIN
                musicbrainz.release_group c ON b.release_group = c.id
                join\s
                musicbrainz.medium d on b.id = d.release
                join musicbrainz.track e on e.medium = d.id
                where a.name = ? and b.name = ?
                order by e.position;""";

        return processTracks(connection, artist, album, returnList, queryString);
    }

    //Added these indexes in order to make it a little faster
    //CREATE INDEX idx_release_group_name_lower ON musicbrainz.release_group ((lower(name)))
    //CREATE INDEX idx_artist_name_lower ON musicbrainz.artist_credit ((lower(name)))
    @Override
    public List<Track> getAlbumTrackListLower(Connection connection, String artist, String album) {
        List<Track> returnList = new ArrayList<>();

        String queryString = """
                SELECT distinct lower(e.name) , e.name as name  , e.position
                FROM\s
                musicbrainz.artist_credit a
                JOIN
                musicbrainz.release b ON a.id = b.artist_credit
                JOIN
                musicbrainz.release_group c ON b.release_group = c.id
                join\s
                musicbrainz.medium d on b.id = d.release
                join musicbrainz.track e on e.medium = d.id
                where lower(a.name) = lower(?) and lower(b.name) = lower(?)
                order by e.position;""";

        return processTracks(connection, artist, album, returnList, queryString);
    }


    @Override
    public List<AlbumInfo> getYearAlbumsByReleaseNameLowerCase(Connection con, List<AlbumInfo> releaseInfo, Year year) {
        String queryString = """
                SELECT DISTINCT
                    (a.name) as artistname, b.name as albumname,  d.first_release_date_year as year\s
                FROM
                    musicbrainz.artist_credit a
                        JOIN
                    musicbrainz.release b ON a.id = b.artist_credit
                        JOIN
                    musicbrainz.release_group c ON b.release_group = c.id
                        JOIN
                    musicbrainz.release_group_meta d ON c.id = d.id\s""";
        String whereSentence;

        StringBuilder artistWhere = new StringBuilder("where lower(a.name) in (");
        StringBuilder albumWhere = new StringBuilder("and lower(b.name) in  (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.substring(0, albumWhere.length() - 1) + ") ";
        whereSentence += "and d.first_release_date_year = ?";

        List<AlbumInfo> returnList = new ArrayList<>();
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {

                preparedStatement.setString(i, albumInfo.getName().toLowerCase());
                preparedStatement.setString(i + releaseInfo.size(), albumInfo.getName().toLowerCase());
                i++;
            }

            prepareRealeaseYearStatement(releaseInfo, year, returnList, preparedStatement);
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<String> getArtistFromCountry(Connection connection, CountryCode country, List<ArtistInfo> allUserArtist) {
        List<String> mbidList = new ArrayList<>();
        StringBuilder queryString = new StringBuilder("""
                SELECT a.gid as mbiz
                FROM musicbrainz.artist a join musicbrainz.area b  on a.area = b.id
                left join musicbrainz.iso_3166_1 c on b.id=c.area  and b.type = 1
                left join country_lookup d on b.id = d.id and b.type != 1
                where  (c.code = ? or d.country =?) and a.gid in (
                """);

        for (ArtistInfo ignored : allUserArtist) {
            queryString.append(" ? ,");
        }

        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");


        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;
            preparedStatement.setString(i++, country.getAlpha2());
            preparedStatement.setString(i++, country.getAlpha2());

            for (ArtistInfo albumInfo : allUserArtist) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String mbid = resultSet.getString("mbiz");
                mbidList.add(mbid);
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return mbidList;

    }


    @Override
    public List<AlbumInfo> getAlbumsOfGenreByName(Connection con, List<AlbumInfo> releaseInfo, String genre) {
        String queryString = """
                SELECT DISTINCT
                    (a.name) as artistname, b.name as albumname\s
                FROM
                    musicbrainz.artist_credit a
                        JOIN
                    musicbrainz.release b ON a.id = b.artist_credit
                        JOIN
                    musicbrainz.release_group c ON b.release_group = c.id
                       JOIN
                    musicbrainz.release_group_tag d ON c.id = b.release_group
                        JOIN
                    musicbrainz.tag e ON d.tag = e.id
                """;
        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.substring(0, albumWhere.length() - 1) + ") ";
        whereSentence += "\n  and similarity(c.name,?) > 0.4";


        List<AlbumInfo> returnList = new ArrayList<>();
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {

                preparedStatement.setString(i, albumInfo.getName());
                preparedStatement.setString(i + releaseInfo.size(), albumInfo.getName());
                i++;
            }
            preparedStatement.setString(1 + releaseInfo.size() * 2, genre);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String artist = resultSet.getString("artistname");
                String album = resultSet.getString("albumname");

                AlbumInfo ai = new AlbumInfo("", album, artist);
                returnList.add(ai);
            }

        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public Set<String> getAlbumsOfGenre(Connection connection, String genre, List<AlbumInfo> releaseInfo) {
        Set<String> uuids = new HashSet<>();
        StringBuilder queryString = new StringBuilder("""
                SELECT\s
                       d.gid FROM
                 musicbrainz.release d join\s
                 musicbrainz.release_group a  on d.release_group = a.id         JOIN
                    musicbrainz.release_group_tag b ON a.id = b.release_group
                        JOIN
                    musicbrainz.tag c ON b.tag = c.id
                WHERE
                    d.gid in (""");

        for (AlbumInfo ignored : releaseInfo) {
            queryString.append(" ? ,");
        }


        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");
        queryString.append("\n  and similarity(c.name,?) > 0.8");

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            preparedStatement.setString(i, genre);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                UUID id = resultSet.getObject("gid", UUID.class);
                uuids.add(id.toString());
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return uuids;
    }

    @Override
    public Set<String> getArtistOfGenre(Connection connection, String genre, List<ArtistInfo> releaseInfo) {
        Set<String> uuids = new HashSet<>();
        StringBuilder queryString = new StringBuilder("""
                SELECT\s
                       d.gid FROM
                 musicbrainz.artist d join\s
                musicbrainz.artist_tag b ON d.id = b.artist
                        JOIN
                    musicbrainz.tag c ON b.tag = c.id
                WHERE
                    d.gid in (""");

        for (ArtistInfo ignored : releaseInfo) {
            queryString.append(" ? ,");
        }


        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");
        queryString.append("\n  and similarity(c.name,?) > 0.8");

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;

            for (ArtistInfo artistInfo : releaseInfo) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(artistInfo.getMbid()));
            }
            preparedStatement.setString(i, genre);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                UUID id = resultSet.getObject("gid", UUID.class);
                uuids.add(id.toString());
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return uuids;
    }


    @Override
    public List<Track> getAlbumTrackListMbid(Connection connection, String mbid) {
        List<Track> returnList = new ArrayList<>();
        String queryString = """
                SELECT   e.name as name ,a.name as artist  , e.position
                FROM\s
                musicbrainz.artist_credit a
                JOIN
                musicbrainz.release b ON a.id = b.artist_credit
                JOIN
                musicbrainz.release_group c ON b.release_group = c.id
                join\s
                musicbrainz.medium d on b.id = d.release
                join musicbrainz.track e on e.medium = d.id
                where b.gid = ?\s
                order by e.position;""";
        return processTracks(connection, mbid, returnList, queryString);
    }

    @Override
    public List<TrackInfo> getAlbumInfoByName(Connection connection, List<AlbumInfo> urlCapsules) {
        List<TrackInfo> list = new ArrayList<>();
        String tempTable = "CREATE TEMP TABLE IF NOT EXISTS findAlbumByTrackName ( track VARCHAR, artist VARCHAR) ON COMMIT DELETE ROWS;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(tempTable)) {
            preparedStatement.execute();
            String append = "insert into findAlbumByTrackName(artist,track) values (?,?)" +
                    (",(?,?)").repeat(Math.max(0, urlCapsules.size() - 1));
            PreparedStatement preparedStatement1 = connection.prepareStatement(append);
            for (int i = 0; i < urlCapsules.size(); i++) {
                preparedStatement1.setString(2 * i + 1, urlCapsules.get(i).getArtist());
                preparedStatement1.setString(2 * i + 2, urlCapsules.get(i).getName());
            }
            preparedStatement1.execute();
            String queryString = """
                    SELECT a.gid AS mbid,c.gid AS ambid, c.name AS albumname, e.artist AS artistaname,e.track AS trackname \s
                    FROM musicbrainz.track a\s
                    JOIN musicbrainz.medium b ON a.medium = b.id\s
                    JOIN musicbrainz.release c ON b.release = c.id\s
                    JOIN musicbrainz.artist_credit d ON a.artist_credit = d.id\s
                    JOIN findalbumbytrackname e ON a.name = e.track AND d.name =  e.artist""";
            ResultSet resultSet = connection.prepareStatement(queryString).executeQuery();
            while (resultSet.next()) {
                String mbid = resultSet.getString("mbid");
                String string = resultSet.getString("albumName");
                String name = resultSet.getString("artistaName");
                String trackName = resultSet.getString("trackName");
                String albumMid = resultSet.getString("ambid");


                TrackInfo trackInfo = new TrackInfo(name, string, trackName, albumMid);
                trackInfo.setMbid(mbid);
                list.add(trackInfo);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return list;

    }


    @Override
    public void getAlbumInfoByMbid(Connection connection, List<ScrobbledAlbum> urlCapsules) {
        Map<String, ScrobbledAlbum> mbidToAlbum = urlCapsules.stream().collect(Collectors.toMap(ScrobbledAlbum::getAlbumMbid, t -> t,
                (t, t2) -> {
                    t.setAlbum(t.getAlbum().length() > t2.getAlbum().length() ? t2.getAlbum() : t.getAlbum());
                    t.setArtist(t.getArtist().length() > t2.getArtist().length() ? t2.getArtist() : t.getArtist());
                    t.setCount(t.getCount() + t2.getCount());
                    return t;
                }));
        String tempTable = "CREATE temp TABLE IF NOT EXISTS frequencies( mbid uuid) ON COMMIT DELETE ROWS;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(tempTable)) {
            preparedStatement.execute();
            String append = "insert into frequencies(mbid) values (?)" +
                    (",(?)").repeat(Math.max(0, urlCapsules.size() - 1));
            PreparedStatement preparedStatement1 = connection.prepareStatement(append);
            for (int i = 0; i < urlCapsules.size(); i++) {
                preparedStatement1.setObject(i + 1, java.util.UUID.fromString(urlCapsules.get(i).getAlbumMbid()));
            }
            preparedStatement1.execute();

            String queryString = "SELECT a.gid AS mbid,c.gid AS ambid ,c.name AS albumname, e.name AS artistaname  FROM musicbrainz.track a " +
                    "JOIN musicbrainz.medium b ON a.medium = b.id " +
                    "JOIN musicbrainz.release c ON b.release = c.id " +
                    "JOIN musicbrainz.artist_credit e ON a.artist_credit = e.id " +
                    "JOIN frequencies d ON a.gid = d.mbid";
            ResultSet resultSet = connection.prepareStatement(queryString).executeQuery();
            while (resultSet.next()) {
                String mbid = resultSet.getString("mbid");
                String string = resultSet.getString("albumName");
                String almbid = resultSet.getString("ambid");
                ScrobbledAlbum scrobbledAlbum = mbidToAlbum.get(mbid);
                scrobbledAlbum.setAlbum(string);
                scrobbledAlbum.setAlbumMbid(almbid);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }


    private void prepareRealeaseYearStatement(List<AlbumInfo> releaseInfo, Year
            year, List<AlbumInfo> returnList, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1 + releaseInfo.size() * 2, year.get(ChronoField.YEAR));

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            String artist = resultSet.getString("artistname");
            String album = resultSet.getString("albumname");

            AlbumInfo ai = new AlbumInfo("", album, artist);
            returnList.add(ai);
        }
    }

    private void prepareRealeaseDecadeStatement(List<AlbumInfo> releaseInfo, int decade, List<
            AlbumInfo> returnList, PreparedStatement preparedStatement, int numberOfYears) throws SQLException {
        preparedStatement.setInt(1 + releaseInfo.size() * 2, decade);
        preparedStatement.setInt(1 + releaseInfo.size() * 2 + 1, decade + numberOfYears);

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            String artist = resultSet.getString("artistname");
            String album = resultSet.getString("albumname");

            AlbumInfo ai = new AlbumInfo("", album, artist);
            returnList.add(ai);
        }
    }

    private List<Track> processTracks(Connection connection, String artist, String
            album, List<Track> returnList, String queryString) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;

            preparedStatement.setString(i++, artist);
            preparedStatement.setString(i, album);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String trackName = resultSet.getString("name");
                int position = resultSet.getInt("position");

                Track t = new Track(artist, trackName, 0, false, 0);
                t.setPosition(position);
                returnList.add(t);
            }

        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;

    }

    private List<Track> processTracks(Connection connection, String mbid, List<Track> returnList, String
            queryString) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;

            preparedStatement.setObject(i, java.util.UUID.fromString(mbid));

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String trackName = resultSet.getString("name");
                String artist = resultSet.getString("artist");
                int position = resultSet.getInt("position");

                Track t = new Track(artist, trackName, 0, false, 0);
                t.setPosition(position);
                returnList.add(t);
            }

        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;

    }

    @Override
    public ArtistMusicBrainzDetails getArtistInfo(Connection connection, ArtistInfo artistInfo) {

        boolean mbidFlag = artistInfo.getMbid() != null && !artistInfo.getMbid().isBlank();
        String query = """
                SELECT  b.name,(case\s
                \t  when c.type = 1 then d.code
                \t  else coalesce(calculate_country(c.id),'NOTVALID') end) as country FROM artist a\s
                LEFT JOIN gender b ON a.gender = b.id
                 join\s
                 musicbrainz.area c on a.area = c.id    left join musicbrainz.iso_3166_1 d  on c.id=d.area and c.type = 1 WHERE\s
                """;
        if (mbidFlag) {
            query += "a.gid = ?  \n";
        } else {
            query += "a.name = ? \n";
        }


        query += "limit 1;\n";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int i = 1;

            if (mbidFlag) {
                preparedStatement.setObject(i, java.util.UUID.fromString(artistInfo.getMbid()));
            } else {
                preparedStatement.setString(i, artistInfo.getArtist());
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String gender = resultSet.getString("name");
                String code = resultSet.getString("country");
                if (code.equals("NOTVALID")) {
                    return null;
                }
                return new ArtistMusicBrainzDetails(gender, code);
            }

        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return null;
    }

    @Override
    public List<CountWrapper<AlbumInfo>> getYearAlbumsByReleaseNameLowerCaseAverage(Connection
                                                                                            con, List<AlbumInfo> releaseInfo, Year year) {
        String queryString = """
                SELECT DISTINCT
                    (a.name) as artistname, b.name as albumname, (sum(e.length) / count(*)) as av\s
                FROM
                    musicbrainz.artist_credit a
                        JOIN
                    musicbrainz.release b ON a.id = b.artist_credit
                        JOIN
                    musicbrainz.release_group c ON b.release_group = c.id
                        JOIN
                    musicbrainz.release_group_meta d ON c.id = d.id         join musicbrainz.medium f on f.release = a.id\s
                \t\tjoin musicbrainz.track e on f.id = e.medium
                """;
        String whereSentence;

        StringBuilder artistWhere = new StringBuilder("where lower(a.name) in (");
        StringBuilder albumWhere = new StringBuilder("and lower(b.name) in  (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.substring(0, albumWhere.length() - 1) + ") ";
        whereSentence += "and d.first_release_date_year = ?";
        whereSentence += " group by a.name ,b.name";


        List<CountWrapper<AlbumInfo>> returnList = new ArrayList<>();
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {

                preparedStatement.setString(i, albumInfo.getName().toLowerCase());
                preparedStatement.setString(i + releaseInfo.size(), albumInfo.getName().toLowerCase());
                i++;
            }

            prepareRealeaseYearStatementAverage(releaseInfo, year, returnList, preparedStatement);
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<CountWrapper<AlbumInfo>> getYearAlbumsByReleaseNameAverage(Connection
                                                                                   connection, List<AlbumInfo> releaseInfo, Year year) {
        String queryString = """
                SELECT DISTINCT
                    (a.name) as artistname, b.name as albumname, (sum(e.length) / count(*)) as av \s
                FROM
                    musicbrainz.artist_credit a
                        JOIN
                    musicbrainz.release b ON a.id = b.artist_credit
                        JOIN
                    musicbrainz.release_group c ON b.release_group = c.id
                        JOIN
                    musicbrainz.release_group_meta d ON c.id = d.id         join musicbrainz.medium f on f.release = a.id\s
                \t\tjoin musicbrainz.track e on f.id = e.medium
                """;

        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.substring(0, albumWhere.length() - 1) + ") ";
        whereSentence += "and d.first_release_date_year = ?";
        whereSentence += " group by a.name,b.name";


        List<CountWrapper<AlbumInfo>> returnList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {

                preparedStatement.setString(i, albumInfo.getName());
                preparedStatement.setString(i + releaseInfo.size(), albumInfo.getName());
                i++;
            }

            prepareRealeaseYearStatementAverage(releaseInfo, year, returnList, preparedStatement);
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;


    }

    @Override
    public List<AlbumInfo> getDecadeAlbums(Connection connection, List<AlbumInfo> mbiz, int decade,
                                           int numberOfYears) {
        List<AlbumInfo> returnList = new ArrayList<>();

        StringBuilder queryString = new StringBuilder("""
                SELECT\s
                a.name as albumname,a.gid as mbid,b.name artistName
                FROM
                    musicbrainz.release a
                     join musicbrainz.artist_credit b ON a.artist_credit = b.id
                       JOIN
                    musicbrainz.release_group c ON a.release_group = c.id
                        JOIN
                    musicbrainz.release_group_meta d ON c.id = d.id Where (d.first_release_date_year between  ? and ?) and    a.gid in (""");
        for (AlbumInfo ignored : mbiz) {
            queryString.append(" ? ,");
        }
        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;
            preparedStatement.setInt(i++, decade);
            preparedStatement.setInt(i++, decade + numberOfYears);


            for (AlbumInfo albumInfo : mbiz) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String mbid = resultSet.getString("mbid");
                String artist = resultSet.getString("artistName");
                String albumName = resultSet.getString("albumname");
                AlbumInfo ai = new AlbumInfo(mbid, albumName, artist);
                returnList.add(ai);
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;

    }

    @Override
    public List<CountWrapper<AlbumInfo>> getDecadeAverage(Connection connection, List<AlbumInfo> mbiz,
                                                          int decade, int numberOfYears) {
        List<CountWrapper<AlbumInfo>> returnList = new ArrayList<>();

        StringBuilder queryString = new StringBuilder("""
                SELECT\s
                a.name as albumname,a.gid as mbid,b.name artistName,(sum(e.length) / count(*)) as av\s
                FROM
                    musicbrainz.release a
                     join musicbrainz.artist_credit b ON a.artist_credit = b.id
                       JOIN
                    musicbrainz.release_group c ON a.release_group = c.id
                        join musicbrainz.medium f on f.release = a.id\s
                \t\tjoin musicbrainz.track e on f.id = e.medium
                \t\tJOIN
                    musicbrainz.release_group_meta d ON c.id = d.id
                \t
                 Where (d.first_release_date_year between ?  and ?) and     a.gid in (""");
        for (AlbumInfo ignored : mbiz) {
            queryString.append(" ? ,");
        }
        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + " ) group by a.gid,a.name,b.name");

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;
            preparedStatement.setInt(i++, decade);
            preparedStatement.setInt(i++, decade + numberOfYears);


            for (AlbumInfo albumInfo : mbiz) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String mbid = resultSet.getString("mbid");
                String artist = resultSet.getString("artistName");
                String albumName = resultSet.getString("albumname");
                int average = resultSet.getInt("av");

                AlbumInfo ai = new AlbumInfo(mbid, albumName, artist);
                returnList.add(new CountWrapper<>(average, ai));
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<CountWrapper<AlbumInfo>> getYearAlbumsByReleaseNameAverageDecade(Connection
                                                                                         connection, List<AlbumInfo> emptyMbid, int decade, int numberOfYears) {
        String queryString = """
                SELECT DISTINCT
                    (a.name) as artistname, b.name as albumname, (sum(e.length) / count(*)) as av \s
                FROM
                    musicbrainz.artist_credit a
                        JOIN
                    musicbrainz.release b ON a.id = b.artist_credit
                        JOIN
                    musicbrainz.release_group c ON b.release_group = c.id
                        JOIN
                    musicbrainz.release_group_meta d ON c.id = d.id         join musicbrainz.medium f on f.release = a.id\s
                \t\tjoin musicbrainz.track e on f.id = e.medium
                """;

        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : emptyMbid) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.substring(0, albumWhere.length() - 1) + ") ";
        whereSentence += "and (d.first_release_date_year between  ? and ?) ";
        whereSentence += " group by a.name,b.name";


        List<CountWrapper<AlbumInfo>> returnList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (AlbumInfo albumInfo : emptyMbid) {

                preparedStatement.setString(i, albumInfo.getName());
                preparedStatement.setString(i + emptyMbid.size(), albumInfo.getName());
                i++;
            }

            prepareRealeaseDecadeStatementAverage(emptyMbid, decade, returnList, preparedStatement, numberOfYears);
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public Map<Language, Long> getScriptLanguages(Connection connection, List<AlbumInfo> mbiz) {
        Map<Language, Long> returnMap = new HashMap<>();

        StringBuilder queryString = new StringBuilder("""
                SELECT\s
                b.name as language_name,iso_code_3 as code,count(*) as frequency
                FROM
                    musicbrainz.release a
                     join musicbrainz.language b ON a.language = b.id
                 where     a.gid in (""");
        for (AlbumInfo ignored : mbiz) {
            queryString.append(" ? ,");
        }
        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");
        queryString.append(" group by b.name,b.iso_code_3 ");

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;

            for (AlbumInfo albumInfo : mbiz) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String language_name = resultSet.getString("language_name");
                String code = resultSet.getString("code");

                long frequency = resultSet.getLong("frequency");

                returnMap.put(new Language(language_name, code), frequency);
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnMap;
    }

    @Override
    public List<AlbumGenre> getAlbumRecommendationsByGenre(Connection connection, Map<Genre, Integer> map, List<ScrobbledArtist> recs) {
        String queryString = """
                SELECT DISTINCT on (a.name)\s
                    (a.name) as artistname, b.name as albumname,e.name  as genreName\s
                FROM
                    musicbrainz.artist_credit a
                        JOIN
                    musicbrainz.release b ON a.id = b.artist_credit
                        JOIN     musicbrainz.release_group c on b.release_group = c.id
                  join    musicbrainz.release_group_tag d on c.id = d.release_group  join tag e on d.tag = e.id\s""";

        String whereSentence = "";
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder genreWhere = new StringBuilder(" and e.name in (");
        for (ScrobbledArtist ignored : recs) {
            artistWhere.append(" ? ,");
        }
        genreWhere.append("? ,".repeat(map.size()));

        whereSentence += artistWhere.substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += genreWhere.substring(0, genreWhere.length() - 1) + ") ";


        List<AlbumGenre> returnList = new ArrayList<>();
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (ScrobbledArtist albumInfo : recs) {

                preparedStatement.setString(i++, albumInfo.getArtist());
            }
            for (Map.Entry<Genre, Integer> genreIntegerEntry : map.entrySet()) {
                preparedStatement.setString(i++, genreIntegerEntry.getKey().getName());
            }


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String artist = resultSet.getString("artistname");
                String album = resultSet.getString("albumname");
                String genre = resultSet.getString("genreName");


                AlbumGenre ai = new AlbumGenre(artist, album, genre);
                returnList.add(ai);
            }
        } catch (
                SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public MultiValuedMap<Genre, String> genreCountByArtist(Connection connection, List<ArtistInfo> releaseInfo) {
        MultiValuedMap<Genre, String> returnMap = new HashSetValuedHashMap<>();
        StringBuilder queryString = new StringBuilder("""
                SELECT\s
                       c.name as neim,d.gid as mbid
                \s
                 FROM
                 musicbrainz.artist d join\s
                musicbrainz.artist_tag b ON d.id = b.artist
                        JOIN
                    musicbrainz.tag c ON b.tag = c.id
                WHERE
                    d.gid in (""");

        for (ArtistInfo ignored : releaseInfo) {
            queryString.append(" ? ,");
        }

        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;

            for (ArtistInfo albumInfo : releaseInfo) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String genreName = resultSet.getString("neim");
                String mbid = resultSet.getString("mbid");
                Genre genre = new Genre(genreName);
                returnMap.put(genre, mbid);
            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnMap;
    }

    @Override
    public MusicbrainzFullAlbumEntity retrieveAlbumInfo(Connection connection, FullAlbumEntityExtended albumInfo) {
        List<String> tags = new ArrayList<>();
        Year year = null;
        String queryString = """
                SELECT     d.first_release_date_year as year,f.name\s
                FROM
                    musicbrainz.release b         JOIN
                    musicbrainz.release_group c ON b.release_group = c.id
                        JOIN
                    musicbrainz.release_group_meta d ON c.id = d.id    left join  musicbrainz.release_group_tag e ON c.id = e.release_group
                        left JOIN
                    musicbrainz.tag f ON e.tag = f.id
                 where b.gid = ?\s""";


        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setObject(1, java.util.UUID.fromString(albumInfo.getMbid()));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (year == null) {
                    year = Year.of(resultSet.getInt(1));
                }
                tags.add(resultSet.getString(2));
            }
            return new MusicbrainzFullAlbumEntity(albumInfo, tags, year);
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
    }

    private void prepareRealeaseYearStatementAverage(List<AlbumInfo> releaseInfo, Year
            year, List<CountWrapper<AlbumInfo>> returnList, PreparedStatement preparedStatement) throws
            SQLException {
        preparedStatement.setInt(1 + releaseInfo.size() * 2, year.get(ChronoField.YEAR));

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            String artist = resultSet.getString("artistname");
            String album = resultSet.getString("albumname");
            int average = resultSet.getInt("av");

            AlbumInfo ai = new AlbumInfo("", album, artist);
            returnList.add(new CountWrapper<>(average, ai));
            returnList.add(new CountWrapper<>(average, ai));
        }
    }

    private void prepareRealeaseDecadeStatementAverage(List<AlbumInfo> releaseInfo, int decade, List<
            CountWrapper<AlbumInfo>> returnList, PreparedStatement preparedStatement, int numberOfYears) throws
            SQLException {
        preparedStatement.setInt(1 + releaseInfo.size() * 2, decade);
        preparedStatement.setInt(1 + releaseInfo.size() * 2 + 1, numberOfYears + decade);


        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            String artist = resultSet.getString("artistname");
            String album = resultSet.getString("albumname");
            int average = resultSet.getInt("av");

            AlbumInfo ai = new AlbumInfo("", album, artist);
            returnList.add(new CountWrapper<>(average, ai));
            returnList.add(new CountWrapper<>(average, ai));
        }
    }


}
