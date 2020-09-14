package dao.musicbrainz;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.model_objects.specification.Artist;
import core.Chuu;
import core.exceptions.ChuuServiceException;
import dao.entities.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

public class MbizQueriesDaoImpl implements MbizQueriesDao {


    @Override
    public List<CountWrapper<AlbumInfo>> getYearAverage(Connection con, List<AlbumInfo> albumInfos, Year year) {
        List<CountWrapper<AlbumInfo>> returnList = new ArrayList<>();

        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "a.name as albumname,a.gid as mbid,b.name artistName,(sum(e.length) / count(*)) as av \n" +
                "FROM\n" +
                "    musicbrainz.release a\n" +
                "     join musicbrainz.artist_credit b ON a.artist_credit = b.id\n" +
                "       JOIN\n" +
                "    musicbrainz.release_group c ON a.release_group = c.id\n" +
                "        join musicbrainz.medium f on f.release = a.id \n" +
                "\t\tjoin musicbrainz.track e on f.id = e.medium\n" +
                "\t\tJOIN\n" +
                "    musicbrainz.release_group_meta d ON c.id = d.id\n" +
                "\t\n" +
                " Where d.first_release_date_year = ? and " +
                "    a.gid in (");
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }


    @Override
    public List<AlbumInfo> getYearAlbums(Connection con, List<AlbumInfo> albumInfos, Year year) {
        List<AlbumInfo> returnList = new ArrayList<>();

        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "a.name as albumname,a.gid as mbid,b.name artistName\n" +
                "FROM\n" +
                "    musicbrainz.release a\n" +
                "     join musicbrainz.artist_credit b ON a.artist_credit = b.id\n" +
                "       JOIN\n" +
                "    musicbrainz.release_group c ON a.release_group = c.id\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group_meta d ON c.id = d.id" +
                " Where d.first_release_date_year = ? and " +
                "    a.gid in (");
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<AlbumInfo> getYearAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, Year year) {
        String queryString = "SELECT DISTINCT\n" +
                "    (a.name) as artistname, b.name as albumname,  d.first_release_date_year as year \n" +
                "FROM\n" +
                "    musicbrainz.artist_credit a\n" +
                "        JOIN\n" +
                "    musicbrainz.release b ON a.id = b.artist_credit\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group c ON b.release_group = c.id\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group_meta d ON c.id = d.id ";
        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.toString().substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.toString().substring(0, albumWhere.length() - 1) + ") ";
        whereSentence += "and d.first_release_date_year = ?";

        List<AlbumInfo> returnList = new ArrayList<>();
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {

                preparedStatement.setString(i, albumInfo.getName());
                preparedStatement.setString(i + releaseInfo.size(), albumInfo.getName());
                i++;
            }

            prepareRealeaseYearStatement(releaseInfo, year, returnList, preparedStatement);
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<AlbumInfo> getDecadeAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, int decade, int numberOfYears) {
        String queryString = "SELECT DISTINCT\n" +
                "    (a.name) as artistname, b.name as albumname,  d.first_release_date_year as year \n" +
                "FROM\n" +
                "    musicbrainz.artist_credit a\n" +
                "        JOIN\n" +
                "    musicbrainz.release b ON a.id = b.artist_credit\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group c ON b.release_group = c.id\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group_meta d ON c.id = d.id ";
        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.toString().substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.toString().substring(0, albumWhere.length() - 1) + ") ";
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }


    @Override
    public Map<Genre, Integer> genreCount(Connection con, List<AlbumInfo> releaseInfo) {
        Map<Genre, Integer> returnMap = new HashMap<>();
        List<Genre> list = new ArrayList<>();
        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "       c.name as neim, count(*) as count\n \n" +
                " FROM\n" +
                " musicbrainz.release d join \n" +
                " musicbrainz.release_group a " +
                " on d.release_group = a.id " +
                "        JOIN\n" +
                "    musicbrainz.release_group_tag b ON a.id = b.release_group\n" +
                "        JOIN\n" +
                "    musicbrainz.tag c ON b.tag = c.id\n" +
                "WHERE\n" +
                "    d.gid in (");

        for (AlbumInfo ignored : releaseInfo) {
            queryString.append(" ? ,");
        }

        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");
        queryString.append("\n Group by c.name");

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString.toString())) {
            int i = 1;

            for (AlbumInfo albumInfo : releaseInfo) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String mbid = resultSet.getString("neim");
                int count = resultSet.getInt("count");
                Genre genre = new Genre(mbid, "");
                list.add(genre);
                returnMap.put(genre, count);
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return list.stream().collect(Collectors.toMap(genre -> genre, genre -> returnMap.getOrDefault(genre, 0), Integer::sum));

    }

    @Override
    public Map<Country, Integer> countryCount(Connection connection, List<ArtistInfo> releaseInfo) {
        Map<Country, Integer> returnMap = new HashMap<>();
        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "       c.code as code, b.name as neim, count(*) as count\n \n" +
                " FROM\n" +
                " musicbrainz.artist a join \n" +
                " musicbrainz.area b" +
                " join musicbrainz.iso_3166_1 c  on b.id=c.area " +
                " on a.area = b.id" +
                "  WHERE b.type = 1" +
                "	 and " +
                "    a.gid in (");

        for (ArtistInfo ignored : releaseInfo) {
            queryString.append(" ? ,");
        }

        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");
        queryString.append(" \n GROUP BY  b.name,c.code");

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;

            for (ArtistInfo albumInfo : releaseInfo) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String coutryName = resultSet.getString("neim");
                String code = resultSet.getString("code");

                int frequency = resultSet.getInt("count");

                returnMap.put(new Country(coutryName, code), frequency);
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnMap;

    }

    @Override
    public List<Track> getAlbumTrackList(Connection connection, String artist, String album) {
        List<Track> returnList = new ArrayList<>();

        String queryString = "SELECT distinct e.name , e.position\n" +
                "FROM \n" +
                "musicbrainz.artist_credit a\n" +
                "JOIN\n" +
                "musicbrainz.release b ON a.id = b.artist_credit\n" +
                "JOIN\n" +
                "musicbrainz.release_group c ON b.release_group = c.id\n" +
                "join \n" +
                "musicbrainz.medium d on b.id = d.release\n" +
                "join musicbrainz.track e on e.medium = d.id\n" +
                "where a.name = ? and b.name = ?\n" +
                "order by e.position;";

        return processTracks(connection, artist, album, returnList, queryString);
    }

    //Added these indexes in order to make it a little faster
    //CREATE INDEX idx_release_group_name_lower ON musicbrainz.release_group ((lower(name)))
    //CREATE INDEX idx_artist_name_lower ON musicbrainz.artist_credit ((lower(name)))
    @Override
    public List<Track> getAlbumTrackListLower(Connection connection, String artist, String album) {
        List<Track> returnList = new ArrayList<>();

        String queryString = "SELECT distinct lower(e.name) , e.name as name  , e.position\n" +
                "FROM \n" +
                "musicbrainz.artist_credit a\n" +
                "JOIN\n" +
                "musicbrainz.release b ON a.id = b.artist_credit\n" +
                "JOIN\n" +
                "musicbrainz.release_group c ON b.release_group = c.id\n" +
                "join \n" +
                "musicbrainz.medium d on b.id = d.release\n" +
                "join musicbrainz.track e on e.medium = d.id\n" +
                "where lower(a.name) = lower(?) and lower(b.name) = lower(?)\n" +
                "order by e.position;";

        return processTracks(connection, artist, album, returnList, queryString);
    }


    @Override
    public List<AlbumInfo> getYearAlbumsByReleaseNameLowerCase(Connection con, List<AlbumInfo> releaseInfo, Year year) {
        String queryString = "SELECT DISTINCT\n" +
                "    (a.name) as artistname, b.name as albumname,  d.first_release_date_year as year \n" +
                "FROM\n" +
                "    musicbrainz.artist_credit a\n" +
                "        JOIN\n" +
                "    musicbrainz.release b ON a.id = b.artist_credit\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group c ON b.release_group = c.id\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group_meta d ON c.id = d.id ";
        String whereSentence;

        StringBuilder artistWhere = new StringBuilder("where lower(a.name) in (");
        StringBuilder albumWhere = new StringBuilder("and lower(b.name) in  (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.toString().substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.toString().substring(0, albumWhere.length() - 1) + ") ";
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<String> getArtistFromCountry(Connection connection, CountryCode country, List<ArtistInfo> allUserArtist) {
        List<String> mbidList = new ArrayList<>();
        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "        a.gid as mbiz  \n" +
                " FROM\n" +
                " musicbrainz.artist a join \n" +
                " musicbrainz.area b " +
                " on a.area = b.id" +
                " join musicbrainz.iso_3166_1 c  on b.id=c.area " +
                "  WHERE b.type = 1 or b.type" +
                "   and c.code = ? " +
                "   and a.gid in (");

        for (ArtistInfo ignored : allUserArtist) {
            queryString.append(" ? ,");
        }

        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");


        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return mbidList;

    }


    @Override
    public List<AlbumInfo> getAlbumsOfGenreByName(Connection con, List<AlbumInfo> releaseInfo, String genre) {
        String queryString = "SELECT DISTINCT\n" +
                "    (a.name) as artistname, b.name as albumname \n" +
                "FROM\n" +
                "    musicbrainz.artist_credit a\n" +
                "        JOIN\n" +
                "    musicbrainz.release b ON a.id = b.artist_credit\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group c ON b.release_group = c.id\n" +
                "       JOIN\n" +
                "    musicbrainz.release_group_tag d ON c.id = b.release_group\n" +
                "        JOIN\n" +
                "    musicbrainz.tag e ON d.tag = e.id\n";
        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.toString().substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.toString().substring(0, albumWhere.length() - 1) + ") ";
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public Set<String> getArtistOfGenre(Connection connection, String genre, List<AlbumInfo> releaseInfo) {
        Set<String> uuids = new HashSet<>();
        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "       d.gid" +
                " FROM\n" +
                " musicbrainz.release d join \n" +
                " musicbrainz.release_group a " +
                " on d.release_group = a.id " +
                "        JOIN\n" +
                "    musicbrainz.release_group_tag b ON a.id = b.release_group\n" +
                "        JOIN\n" +
                "    musicbrainz.tag c ON b.tag = c.id\n" +
                "WHERE\n" +
                "    d.gid in (");

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
    public List<Track> getAlbumTrackListMbid(Connection connection, String mbid) {
        List<Track> returnList = new ArrayList<>();
        String queryString = "SELECT   e.name as name ,a.name as artist  , e.position\n" +
                "FROM \n" +
                "musicbrainz.artist_credit a\n" +
                "JOIN\n" +
                "musicbrainz.release b ON a.id = b.artist_credit\n" +
                "JOIN\n" +
                "musicbrainz.release_group c ON b.release_group = c.id\n" +
                "join \n" +
                "musicbrainz.medium d on b.id = d.release\n" +
                "join musicbrainz.track e on e.medium = d.id\n" +
                "where b.gid = ? \n" +
                "order by e.position;";
        return processTracks(connection, mbid, returnList, queryString);
    }

    @Override
    public List<TrackInfo> getAlbumInfoByName(Connection connection, List<UrlCapsule> urlCapsules) {
        List<TrackInfo> list = new ArrayList<>();
        String tempTable = "CREATE TEMP TABLE IF NOT EXISTS findAlbumByTrackName ( track VARCHAR, artist VARCHAR) ON COMMIT DELETE ROWS;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(tempTable)) {
            preparedStatement.execute();
            StringBuilder append = new StringBuilder().append("insert into findAlbumByTrackName(artist,track) values (?,?)")
                    .append((",(?,?)").repeat(Math.max(0, urlCapsules.size() - 1)));
            PreparedStatement preparedStatement1 = connection.prepareStatement(append.toString());
            ;
            for (int i = 0; i < urlCapsules.size(); i++) {
                preparedStatement1.setString(2 * i + 1, urlCapsules.get(i).getArtistName());
                preparedStatement1.setString(2 * i + 2, urlCapsules.get(i).getAlbumName());
            }
            preparedStatement1.execute();
            String queryString = "SELECT a.gid AS mbid,c.gid AS ambid, c.name AS albumname, e.artist AS artistaname,e.track AS trackname  \n" +
                    "FROM musicbrainz.track a \n" +
                    "JOIN musicbrainz.medium b ON a.medium = b.id \n" +
                    "JOIN musicbrainz.release c ON b.release = c.id \n" +
                    "JOIN musicbrainz.artist_credit d ON a.artist_credit = d.id \n" +
                    "JOIN findalbumbytrackname e ON a.name = e.track AND d.name =  e.artist";
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
    public void getAlbumInfoByMbid(Connection connection, List<UrlCapsule> urlCapsules) {
        Map<String, UrlCapsule> collect = urlCapsules.stream().collect(Collectors.toMap(UrlCapsule::getMbid, t -> t,
                (t, t2) -> {
                    t.setAlbumName(t.getAlbumName().length() > t2.getAlbumName().length() ? t2.getAlbumName() : t.getAlbumName());
                    t.setArtistName(t.getArtistName().length() > t2.getArtistName().length() ? t2.getArtistName() : t.getArtistName());
                    t.setPlays(t.getPlays() + t2.getPlays());
                    return t;
                }));
        String tempTable = "CREATE temp TABLE IF NOT EXISTS frequencies( mbid uuid) ON COMMIT DELETE ROWS;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(tempTable)) {
            preparedStatement.execute();
            StringBuilder append = new StringBuilder().append("insert into frequencies(mbid) values (?)")
                    .append((",(?)").repeat(Math.max(0, urlCapsules.size() - 1)));
            PreparedStatement preparedStatement1 = connection.prepareStatement(append.toString());
            for (int i = 0; i < urlCapsules.size(); i++) {
                preparedStatement1.setObject(i + 1, java.util.UUID.fromString(urlCapsules.get(i).getMbid()));
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
                UrlCapsule urlCapsule = collect.get(mbid);
                urlCapsule.setAlbumName(string);
                urlCapsule.setMbid(almbid);
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;

    }

    private List<Track> processTracks(Connection connection, String mbid, List<Track> returnList, String
            queryString) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;

            preparedStatement.setObject(i++, java.util.UUID.fromString(mbid));

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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;

    }

    @Override
    public ArtistMusicBrainzDetails getArtistInfo(Connection connection, ArtistInfo artistInfo) {

        boolean mbidFlag = artistInfo.getMbid() != null && !artistInfo.getMbid().isBlank();
        String query = "SELECT  b.name,d.code FROM artist a \n" +
                "LEFT JOIN gender b ON a.gender = b.id\n" +
                "LEFT JOIN area c ON a.area = c.id\n" +
                "LEFT JOIN iso_3166_1 d ON c.id = d.area \n" +
                "WHERE \n";
        if (mbidFlag) {
            query += "a.gid = ?  \n";
        } else {
            query += "a.name = ? \n";
        }


        query += "limit 1;\n";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int i = 1;

            if (mbidFlag) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(artistInfo.getMbid()));
            } else {
                preparedStatement.setString(i, artistInfo.getArtist());
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String gender = resultSet.getString("name");
                String code = resultSet.getString("code");

                return new ArtistMusicBrainzDetails(gender, code);
            }

        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return null;
    }

    @Override
    public List<CountWrapper<AlbumInfo>> getYearAlbumsByReleaseNameLowerCaseAverage(Connection
                                                                                            con, List<AlbumInfo> releaseInfo, Year year) {
        String queryString = "SELECT DISTINCT\n" +
                "    (a.name) as artistname, b.name as albumname, (sum(e.length) / count(*)) as av \n" +
                "FROM\n" +
                "    musicbrainz.artist_credit a\n" +
                "        JOIN\n" +
                "    musicbrainz.release b ON a.id = b.artist_credit\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group c ON b.release_group = c.id\n" +
                "        JOIN\n" +

                "    musicbrainz.release_group_meta d ON c.id = d.id " +
                "        join musicbrainz.medium f on f.release = a.id \n" +
                "\t\tjoin musicbrainz.track e on f.id = e.medium\n";
        String whereSentence;

        StringBuilder artistWhere = new StringBuilder("where lower(a.name) in (");
        StringBuilder albumWhere = new StringBuilder("and lower(b.name) in  (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.toString().substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.toString().substring(0, albumWhere.length() - 1) + ") ";
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<CountWrapper<AlbumInfo>> getYearAlbumsByReleaseNameAverage(Connection
                                                                                   connection, List<AlbumInfo> releaseInfo, Year year) {
        String queryString = "SELECT DISTINCT\n" +
                "    (a.name) as artistname, b.name as albumname, (sum(e.length) / count(*)) as av  \n" +
                "FROM\n" +
                "    musicbrainz.artist_credit a\n" +
                "        JOIN\n" +
                "    musicbrainz.release b ON a.id = b.artist_credit\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group c ON b.release_group = c.id\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group_meta d ON c.id = d.id " +
                "        join musicbrainz.medium f on f.release = a.id \n" +
                "\t\tjoin musicbrainz.track e on f.id = e.medium\n";

        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : releaseInfo) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.toString().substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.toString().substring(0, albumWhere.length() - 1) + ") ";
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;


    }

    @Override
    public List<AlbumInfo> getDecadeAlbums(Connection connection, List<AlbumInfo> mbiz, int decade,
                                           int numberOfYears) {
        List<AlbumInfo> returnList = new ArrayList<>();

        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "a.name as albumname,a.gid as mbid,b.name artistName\n" +
                "FROM\n" +
                "    musicbrainz.release a\n" +
                "     join musicbrainz.artist_credit b ON a.artist_credit = b.id\n" +
                "       JOIN\n" +
                "    musicbrainz.release_group c ON a.release_group = c.id\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group_meta d ON c.id = d.id" +
                " Where (d.first_release_date_year between  ? and ?) and" +
                "    a.gid in (");
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;

    }

    @Override
    public List<CountWrapper<AlbumInfo>> getDecadeAverage(Connection connection, List<AlbumInfo> mbiz,
                                                          int decade, int numberOfYears) {
        List<CountWrapper<AlbumInfo>> returnList = new ArrayList<>();

        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "a.name as albumname,a.gid as mbid,b.name artistName,(sum(e.length) / count(*)) as av \n" +
                "FROM\n" +
                "    musicbrainz.release a\n" +
                "     join musicbrainz.artist_credit b ON a.artist_credit = b.id\n" +
                "       JOIN\n" +
                "    musicbrainz.release_group c ON a.release_group = c.id\n" +
                "        join musicbrainz.medium f on f.release = a.id \n" +
                "\t\tjoin musicbrainz.track e on f.id = e.medium\n" +
                "\t\tJOIN\n" +
                "    musicbrainz.release_group_meta d ON c.id = d.id\n" +
                "\t\n" +
                " Where (d.first_release_date_year between ?  and ?) and " +
                "    a.gid in (");
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<CountWrapper<AlbumInfo>> getYearAlbumsByReleaseNameAverageDecade(Connection
                                                                                         connection, List<AlbumInfo> emptyMbid, int decade, int numberOfYears) {
        String queryString = "SELECT DISTINCT\n" +
                "    (a.name) as artistname, b.name as albumname, (sum(e.length) / count(*)) as av  \n" +
                "FROM\n" +
                "    musicbrainz.artist_credit a\n" +
                "        JOIN\n" +
                "    musicbrainz.release b ON a.id = b.artist_credit\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group c ON b.release_group = c.id\n" +
                "        JOIN\n" +
                "    musicbrainz.release_group_meta d ON c.id = d.id " +
                "        join musicbrainz.medium f on f.release = a.id \n" +
                "\t\tjoin musicbrainz.track e on f.id = e.medium\n";

        String whereSentence;
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder albumWhere = new StringBuilder("and b.name in (");
        for (AlbumInfo ignored : emptyMbid) {
            artistWhere.append(" ? ,");
            albumWhere.append(" ? ,");
        }
        whereSentence = artistWhere.toString().substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += albumWhere.toString().substring(0, albumWhere.length() - 1) + ") ";
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public Map<Language, Long> getScriptLanguages(Connection connection, List<AlbumInfo> mbiz) {
        Map<Language, Long> returnMap = new HashMap<>();

        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "b.name as language_name,iso_code_3 as code,count(*) as frequency\n" +
                "FROM\n" +
                "    musicbrainz.release a\n" +
                "     join musicbrainz.language b ON a.language = b.id\n" +
                " where " +
                "    a.gid in (");
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnMap;
    }

    @Override
    public List<AlbumGenre> getAlbumRecommendationsByGenre(Connection connection, Map<Genre, Integer> map, List<ScrobbledArtist> recs) {
        String queryString = "SELECT DISTINCT on (a.name) \n" +
                "    (a.name) as artistname, b.name as albumname,e.name  as genreName \n" +
                "FROM\n" +
                "    musicbrainz.artist_credit a\n" +
                "        JOIN\n" +
                "    musicbrainz.release b ON a.id = b.artist_credit\n" +
                "        JOIN" +
                "     musicbrainz.release_group c on b.release_group = c.id\n" +
                "  join " +
                "   musicbrainz.release_group_tag d on c.id = d.release_group " +
                " join tag e on d.tag = e.id ";

        String whereSentence = "";
        StringBuilder artistWhere = new StringBuilder("where a.name in (");
        StringBuilder genreWhere = new StringBuilder(" and e.name in (");
        for (ScrobbledArtist ignored : recs) {
            artistWhere.append(" ? ,");
        }
        for (int i = 0; i < map.size(); i++) {
            genreWhere.append("? ,");
        }

        whereSentence += artistWhere.toString().substring(0, artistWhere.length() - 1) + ") ";
        whereSentence += genreWhere.toString().substring(0, genreWhere.length() - 1) + ") ";


        List<AlbumGenre> returnList = new ArrayList<>();
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(queryString + whereSentence)) {
            int i = 1;

            for (ScrobbledArtist albumInfo : recs) {

                preparedStatement.setString(i++, albumInfo.getArtist());
            }
            for (Map.Entry<Genre, Integer> genreIntegerEntry : map.entrySet()) {
                preparedStatement.setString(i++, genreIntegerEntry.getKey().getGenreName());
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public Map<Genre, Integer> genreCountByArtist(Connection connection, List<ArtistInfo> releaseInfo) {
        Map<Genre, Integer> returnMap = new HashMap<>();
        List<Genre> list = new ArrayList<>();
        StringBuilder queryString = new StringBuilder("SELECT \n" +
                "       c.name as neim, count(*) as count\n \n" +
                " FROM\n" +
                " musicbrainz.artist d join \n" +
                "musicbrainz.artist_tag b ON d.id = b.artist\n" +
                "        JOIN\n" +
                "    musicbrainz.tag c ON b.tag = c.id\n" +
                "WHERE\n" +
                "    d.gid in (");

        for (ArtistInfo ignored : releaseInfo) {
            queryString.append(" ? ,");
        }

        queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");
        queryString.append("\n Group by c.name");

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
            int i = 1;

            for (ArtistInfo albumInfo : releaseInfo) {
                preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
            }
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String mbid = resultSet.getString("neim");
                int count = resultSet.getInt("count");
                Genre genre = new Genre(mbid, "");
                list.add(genre);
                returnMap.put(genre, count);
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return list.stream().collect(Collectors.toMap(genre -> genre, genre -> returnMap.getOrDefault(genre, 0), Integer::sum));
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