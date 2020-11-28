package dao;

import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;
import org.intellij.lang.annotations.Language;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TrackDaoImpl extends BaseDAO implements TrackDao {

    private String prepareINQuerySingle(int size) {
        return String.join(",", Collections.nCopies(size, "(?)"));
    }

    private String prepareINQueryTuple(int size) {
        return String.join(",", Collections.nCopies(size, "(?,?)"));
    }

    @Override
    public void fillIdsMbids(Connection connection, List<ScrobbledTrack> list) {
        String queryString = "SELECT id,mbid FROM track WHERE  mbid in (%s)  ";

        String sql = String.format(queryString, list.isEmpty() ? null : prepareINQuerySingle(list.size()));

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setString(i + 1, list.get(i).getMbid());
            }

            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, ScrobbledTrack> secundaryMap = new HashMap<>();
            Map<String, ScrobbledTrack> trackMap = list.stream().collect(Collectors.toMap(ScrobbledTrack::getMbid, Function.identity(), (scrobbledArtist, scrobbledArtist2) -> {
                secundaryMap.put(scrobbledArtist2.getMbid(), scrobbledArtist2);
                return scrobbledArtist;
            }));
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String mbid = resultSet.getString("mbid");
                ScrobbledTrack scrobbledTrack = trackMap.get(mbid);
                if (scrobbledTrack != null) {
                    scrobbledTrack.setTrackId(id);
                }
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public @Nullable
    FullAlbumEntity getAlbumTrackList(Connection connection, long albumId, String lastfmId) {
        List<Track> tracks = new ArrayList<>();
        FullAlbumEntity fullAlbumEntity = null;

        String mySql = "Select d.name,b.album_name,c.duration,c.track_name,coalesce(c.url,b.url,d.url),e.playnumber,e.loved " +
                "from album_tracklist a join album b on a.album_id =b.id join track c on a.track_id = c.id join artist d on a.artist_id = d.id" +
                " left join scrobbled_track e on a.track_id = e.id  where album_id = ? and lastfm_id = ?  order by a.position asc";
        try
                (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setLong(1, albumId);
            preparedStatement.setString(2, lastfmId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                String artsitName = resultSet.getString(1);
                String albumName = resultSet.getString(2);
                int duration = resultSet.getInt(3);
                String trackName = resultSet.getString(4);
                String url = resultSet.getString(5);
                int plays = resultSet.getInt(6);
                boolean loved = resultSet.getBoolean(7);
                if (fullAlbumEntity == null) {
                    fullAlbumEntity = new FullAlbumEntity(artsitName, albumName, 0, url, lastfmId);
                    fullAlbumEntity.setTrackList(tracks);
                }
                tracks.add(new Track(artsitName, trackName, plays, loved, duration));
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return fullAlbumEntity;

    }

    @Override
    public List<ScrobbledTrack> getUserTopTracks(Connection connection, String lastfmid) {
        List<ScrobbledTrack> scrobbledTracks = new ArrayList<>();

        String mySql = "Select b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),a.playnumber,a.loved " +
                "from scrobbled_track a join track b on a.track_id = b.id join artist c on b.artist_id = c.id left join album d on b.album_id = d.id where a.lastfm_id = ? order by playnumber desc";
        try
                (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setString(1, lastfmid);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long trackId = resultSet.getLong(1);
                long albumId = resultSet.getLong(2);
                long artistId = resultSet.getLong(3);
                String artsitName = resultSet.getString(4);
                String albumName = resultSet.getString(5);
                int duration = resultSet.getInt(6);
                String trackName = resultSet.getString(7);
                String url = resultSet.getString(8);
                int plays = resultSet.getInt(9);
                boolean loved = resultSet.getBoolean(10);

                ScrobbledTrack e = new ScrobbledTrack(artsitName, trackName, plays, loved, duration, url, null, null);
                e.setArtistId(artistId);
                e.setAlbumId(albumId);
                e.setTrackId(trackId);
                scrobbledTracks.add(e);
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return scrobbledTracks;
    }

    @Override
    public List<ScrobbledTrack> getTopSpotifyTracksIds(Connection connection, String lastfmId, int limit) {
        List<ScrobbledTrack> scrobbledTracks = new ArrayList<>();

        String mySql = "Select b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),a.playnumber,a.loved,b.spotify_id " +
                "from scrobbled_track a join track b on a.track_id = b.id join artist c on b.artist_id = c.id left join album d on b.album_id = d.id where a.lastfm_id = ? and b.spotify_id is not null order by playnumber desc limit ? ";
        try
                (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setString(1, lastfmId);
            preparedStatement.setInt(2, limit);


            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long trackId = resultSet.getLong(1);
                long albumId = resultSet.getLong(2);
                long artistId = resultSet.getLong(3);
                String artsitName = resultSet.getString(4);
                String albumName = resultSet.getString(5);
                int duration = resultSet.getInt(6);
                String trackName = resultSet.getString(7);
                String url = resultSet.getString(8);
                int plays = resultSet.getInt(9);
                boolean loved = resultSet.getBoolean(10);
                String spotifyId = resultSet.getString(11);

                ScrobbledTrack e = new ScrobbledTrack(artsitName, trackName, plays, loved, duration, url, null, null);
                e.setArtistId(artistId);
                e.setAlbumId(albumId);
                e.setTrackId(trackId);
                e.setSpotifyId(spotifyId);
                scrobbledTracks.add(e);
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return scrobbledTracks;
    }

    @Override
    public List<ScrobbledTrack> getUserTopTracksNoSpotifyId(Connection connection, String lastfmid, int limit) {
        List<ScrobbledTrack> scrobbledTracks = new ArrayList<>();

        String mySql = "Select b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),a.playnumber,a.loved " +
                "from scrobbled_track a join track b on a.track_id = b.id join artist c on b.artist_id = c.id left join album d on b.album_id = d.id where a.lastfm_id = ? and b.spotify_id is null order by playnumber desc limit ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setString(1, lastfmid);
            preparedStatement.setInt(2, limit);


            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long trackId = resultSet.getLong(1);
                long albumId = resultSet.getLong(2);
                long artistId = resultSet.getLong(3);
                String artsitName = resultSet.getString(4);
                String albumName = resultSet.getString(5);
                int duration = resultSet.getInt(6);
                String trackName = resultSet.getString(7);
                String url = resultSet.getString(8);
                int plays = resultSet.getInt(9);
                boolean loved = resultSet.getBoolean(10);
                ScrobbledTrack e = new ScrobbledTrack(artsitName, trackName, plays, loved, duration, url, null, null);
                e.setArtistId(artistId);
                e.setAlbumId(albumId);
                e.setTrackId(trackId);
                scrobbledTracks.add(e);
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return scrobbledTracks;
    }


    @Override
    public void fillIds(Connection connection, List<ScrobbledTrack> list) {
        if (list.isEmpty()) {
            return;
        }
        String queryString = "SELECT id,artist_id,track_name FROM track USE INDEX (track_and_artist) WHERE  (artist_id,track_name) in  (%s)  ";

        String sql = String.format(queryString, prepareINQueryTuple(list.size()));

        UUID a = UUID.randomUUID();
        String seed = a.toString();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setLong(2 * i + 1, list.get(i).getArtistId());
                preparedStatement.setString(2 * i + 2, list.get(i).getName());
            }

            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, ScrobbledTrack> trackMap = list.stream().collect(Collectors.toMap(scrobbledTrack -> scrobbledTrack.getArtistId() + "_" + seed + "_" + scrobbledTrack.getName().toLowerCase(), Function.identity(), (scrobbledArtist, scrobbledArtist2) -> {
                scrobbledArtist.setCount(scrobbledArtist.getCount() + scrobbledArtist2.getCount());
                return scrobbledArtist;
            }));
            Pattern compile = Pattern.compile("\\p{M}");

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                long artist_id = resultSet.getLong("artist_id");

                String name = resultSet.getString("track_name");
                ScrobbledTrack scrobbledTrack = trackMap.get(artist_id + "_" + seed + "_" + name.toLowerCase());
                if (scrobbledTrack != null) {
                    scrobbledTrack.setTrackId(id);
                } else {
                    // name can be stripped or maybe the element is collect is the stripped one
                    String normalizeArtistName = compile.matcher(
                            Normalizer.normalize(name, Normalizer.Form.NFKD)
                    ).replaceAll("");
                    ScrobbledTrack normalizedArtist = trackMap.get(artist_id + "_" + seed + "_" + normalizeArtistName.toLowerCase());
                    if (normalizedArtist != null) {
                        normalizedArtist.setTrackId(id);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteAllUserTracks(Connection con, String lastfmId) {
        @Language("MariaDB") String queryString = "DELETE   FROM scrobbled_track  WHERE lastfm_id = ? ";
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
    public void insertTracks(Connection connection, List<ScrobbledTrack> nonExistingId) {
        StringBuilder mySql =
                new StringBuilder("INSERT INTO track (artist_id,track_name,url,mbid,duration) VALUES (?,?,?,?,?)");

        mySql.append(",(?,?,?,?)".repeat(Math.max(0, nonExistingId.size() - 1)));
        mySql.append(" on duplicate key update " +
                " mbid = if(mbid is null and values(mbid) is not null,values(mbid),mbid), " +
                " duration = if(duration is null and values(duration) is not null,values(duration),duration), " +
                " url = if(url is null and values(url) is not null,values(url),url)  returning id ");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            for (int i = 0; i < nonExistingId.size(); i++) {
                ScrobbledTrack x = nonExistingId.get(i);
                preparedStatement.setLong(5 * i + 1, x.getArtistId());
                preparedStatement.setString(5 * i + 2, x.getName());
                preparedStatement.setString(5 * i + 3, x.getUrl());
                String trackMbid = x.getMbid();
                if (trackMbid == null || trackMbid.isBlank()) {
                    trackMbid = null;
                }
                preparedStatement.setString(5 * i + 4, trackMbid);
                preparedStatement.setInt(5 * i + 5, x.getDuration());


            }
            preparedStatement.execute();

            ResultSet ids = preparedStatement.getResultSet();
            int counter = 0;
            while (ids.next()) {
                long aLong = ids.getLong(1);
                nonExistingId.get(counter++).setTrackId(aLong);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertTrack(Connection connection, ScrobbledTrack x) {
        String sql = "INSERT INTO track (artist_id,track_name,url,mbid,duration) VALUES (?,?,?,?,?) on duplicate key update" +
                " mbid = if(mbid is null and values(mbid) is not null,values(mbid),mbid), " +
                " duration = if(duration is null and values(duration) is not null,values(duration),duration), " +
                " url = if(url is null and values(url) is not null,values(url),url) returning id ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(+1, x.getArtistId());
            String track = x.getName();
            preparedStatement.setString(2, track);
            preparedStatement.setString(3, x.getUrl());
            String trackMbid = x.getMbid();
            if (trackMbid == null || trackMbid.isBlank()) {
                trackMbid = null;
            }
            preparedStatement.setString(4, trackMbid);
            preparedStatement.setInt(5, x.getDuration());


            preparedStatement.execute();
            ResultSet ids = preparedStatement.getResultSet();
            if (ids.next()) {
                x.setTrackId(ids.getLong(1));
            } else {
                try {
                    if (track.length() > 400) {
                        track = track.substring(0, 400);
                    }
                    long trackId = getTrackIdByName(connection, track, x.getArtistId());
                    x.setTrackId(trackId);
                } catch (InstanceNotFoundException e) {
                    logger.warn("ERROR CREATING {} {} {}", x, track, x.getArtist());
                    //throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public long getTrackIdByName(Connection connection, String track, long artistId) throws InstanceNotFoundException {
        @Language("MariaDB") String queryString = "SELECT id FROM  track WHERE track_name = ? and artist_id = ?  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(2, artistId);
            preparedStatement.setString(1, track);
            ResultSet execute = preparedStatement.executeQuery();
            if (execute.next()) {
                return execute.getLong(1);
            }
            throw new InstanceNotFoundException(track);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public ScrobbledTrack getTrackByName(Connection connection, String track, long artistId) throws InstanceNotFoundException {
//        @Language("MariaDB") String queryString = "SELECT id,artist_id,album_name,url,duration,mbid,spotify_id FROM  album WHERE album_name = ? and artist_id = ?  ";
//        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
//            preparedStatement.setLong(2, artistId);
//            preparedStatement.setString(1, track);
//
//
//            ResultSet execute = preparedStatement.executeQuery();
//            if (execute.next()) {
//                long id = execute.getLong(1);
//                long artist_id_ = execute.getLong(2);
//                String albumName = execute.getString(3);
//                String url = execute.getString(4);
//                long rym_id = execute.getLong(5);
//                String mbid = execute.getString(6);
//                String spotify_id = execute.getString(7);
//
//
//                return null;
////                new Tr(id, artistId, albumName, url, rym_id, UUID.fromString(mbid), spotify_id);
//            }
//            throw new InstanceNotFoundException(track);
//        } catch (SQLException e) {
//            throw new ChuuServiceException(e);
//        }
        return null;
    }

    @Override
    public void addSrobbledTracks(Connection con, List<ScrobbledTrack> scrobbledTracks) {
        /* Create "queryString". */

        StringBuilder mySql =
                new StringBuilder("INSERT INTO  scrobbled_track" +
                        "                  (artist_id,track_id,lastfm_id,playnumber,loved) VALUES (?,?,?,?,?) ");

        mySql.append(", (?,?,?,?,?)".repeat(Math.max(0, scrobbledTracks.size() - 1)));
        mySql.append(" ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber, loved = VALUES(loved)");

        try {
            PreparedStatement preparedStatement = con.prepareStatement(mySql.toString());
            for (int i = 0; i < scrobbledTracks.size(); i++) {
                ScrobbledTrack scrobbledTrack = scrobbledTracks.get(i);
                preparedStatement.setLong(5 * i + 1, scrobbledTrack.getArtistId());
                preparedStatement.setLong(5 * i + 2, scrobbledTrack.getTrackId());
                preparedStatement.setString(5 * i + 3, scrobbledTrack.getDiscordID());
                preparedStatement.setInt(5 * i + 4, scrobbledTrack.getCount());
                preparedStatement.setBoolean(5 * i + 5, scrobbledTrack.isLoved());
            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<AlbumUserPlays> getUserTopArtistTracks(Connection connection, long discord_id, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "select a.playnumber,b.track_name,d.name,b.url from scrobbled_track a join track b on a.track_id = b.id join user c on a.lastfm_id = c.lastfm_id " +
                "join artist d on b.artist_id = d.id  where c.discord_id = ? and a.artist_id = ?  order by a.playnumber desc  limit ? ";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setLong(i++, discord_id);
            preparedStatement.setLong(i++, artistId);
            processArtistTracks(limit, returnList, preparedStatement, i);


        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<AlbumUserPlays> getServerTopArtistTracks(Connection connection, long guildId, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "select sum(a.playnumber) as ord,b.track_name,d.name,b.url from scrobbled_track a join track b on a.track_id = b.id join user c on a.lastfm_id = c.lastfm_id" +
                " join user_guild e on c.discord_id = e.discord_id " +
                "join artist d on b.artist_id = d.id  where e.guild_id = ? and a.artist_id = ?  " +
                " group by a.track_id " +
                "order by ord desc  limit ? ";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setLong(i++, guildId);
            preparedStatement.setLong(i++, artistId);
            processArtistTracks(limit, returnList, preparedStatement, i);


        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<AlbumUserPlays> getGlobalTopArtistTracks(Connection connection, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "select sum(a.playnumber) as ord,b.track_name,d.name,b.url from scrobbled_track a join track b on a.track_id = b.id" +
                " join artist d on b.artist_id = d.id  where  a.artist_id = ?" +
                " group by a.track_id " +
                "  order by ord desc  limit ? ";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setLong(i++, artistId);
            processArtistTracks(limit, returnList, preparedStatement, i);
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    private void processArtistTracks(int limit, List<AlbumUserPlays> returnList, PreparedStatement preparedStatement, int i) throws SQLException {
        preparedStatement.setInt(i, limit);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            int plays = resultSet.getInt(1);
            String trackName = resultSet.getString(2);
            String artist = resultSet.getString(3);
            String url = resultSet.getString(4);

            AlbumUserPlays e = new AlbumUserPlays(trackName, url);
            e.setArtist(artist);
            e.setPlays(plays);
            returnList.add(e);
        }
    }

    @Override
    public Map<Genre, Integer> genreCountsByTracks(Connection connection, List<AlbumInfo> albumInfos) {
//        @Language("MariaDB") String queryString = "SELECT tag,count(*) as coun FROM album a join artist b on a.artist_id =b.id join album_tags c  on a.id = c.album_id WHERE (name) in (%s) and (album_name)  IN (%s) group by tag";
//        String sql = String.format(queryString, albumInfos.isEmpty() ? null : prepareINQuerySingle(albumInfos.size()), albumInfos.isEmpty() ? null : prepareINQuerySingle(albumInfos.size()));
//
//
//        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//            for (int i = 0; i < albumInfos.size(); i++) {
//                preparedStatement.setString(i + 1, albumInfos.get(i).getArtist());
//                preparedStatement.setString(i + 1 + albumInfos.size(), albumInfos.get(i).getName());
//            }
//
//            /* Fill "preparedStatement". */
//
//
//            Map<Genre, Integer> returnList = new HashMap<>();
//            /* Execute query. */
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//
//                String tag = resultSet.getString("tag");
//                int count = resultSet.getInt("coun");
//
//                returnList.put(new Genre(tag, null), count);
//
//            }
//            return returnList;
//        } catch (SQLException e) {
//            throw new ChuuServiceException(e);
//        }
        return null;
    }

    @Override
    public String getTrackUrlByName(Connection connection, String name, long artistId) throws InstanceNotFoundException {
//        @Language("MariaDB") String queryString = "SELECT url FROM  album WHERE album_name = ? and artist_id = ?  ";
//        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
//            preparedStatement.setLong(2, artistId);
//            preparedStatement.setString(1, name);
//
//
//            ResultSet execute = preparedStatement.executeQuery();
//            if (execute.next()) {
//                return execute.getString(1);
//            }
//            throw new InstanceNotFoundException(name);
//        } catch (SQLException e) {
//            throw new ChuuServiceException(e);
//        }
        return null;
    }


}
