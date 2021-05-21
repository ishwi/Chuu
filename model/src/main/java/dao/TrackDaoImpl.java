package dao;

import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;

import javax.annotation.Nullable;
import java.sql.*;
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
    public Optional<Album> findAlbumFromTrack(Connection connection, long trackId) {
        String queryString = """
                SELECT *
                                     FROM (
                                              SELECT album_name, coalesce(a.url, a2.url)
                                              FROM track a
                                                       JOIN album a2 ON a.album_id = a2.id
                                              WHERE a.id = ?
                                              UNION
                                              SELECT album_name, coalesce(c.url, a3.url)
                                              FROM track c
                                                       JOIN album_tracklist a2 ON c.id = a2.track_id
                                                       JOIN album a3 ON a2.album_id = a3.id
                                              WHERE c.id = ?) main
                                     LIMIT 1
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            preparedStatement.setLong(1, trackId);
            preparedStatement.setLong(2, trackId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString(1);
                String url = resultSet.getString(2);
                return Optional.of(new Album(-1, -1, name, url, null, null, null));
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return Optional.empty();
    }

    @Override
    public @Nullable
    Optional<FullAlbumEntity> getAlbumTrackList(Connection connection, long albumId, String lastfmId) {
        List<Track> tracks = new ArrayList<>();
        FullAlbumEntity fullAlbumEntity = null;

        String mySql = "SELECT d.name,b.album_name,c.duration,c.track_name,coalesce(c.url,b.url,d.url),coalesce(e.playnumber,0),coalesce(e.loved,FALSE),a.position " +
                       "FROM album_tracklist a JOIN album b ON a.album_id =b.id JOIN track c ON a.track_id = c.id JOIN artist d ON c.artist_id = d.id" +
                       " LEFT JOIN (SELECT * FROM scrobbled_track WHERE lastfm_id = ? ) e ON a.track_id = e.track_id  WHERE a.album_id = ?   ORDER BY a.position ASC";
        try
                (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setString(1, lastfmId);
            preparedStatement.setLong(2, albumId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                String artsitName = resultSet.getString(1);
                String albumName = resultSet.getString(2);
                int duration = resultSet.getInt(3);
                String trackName = resultSet.getString(4);
                String url = resultSet.getString(5);
                int plays = resultSet.getInt(6);
                boolean loved = resultSet.getBoolean(7);
                int position = resultSet.getInt(8);

                if (fullAlbumEntity == null) {
                    fullAlbumEntity = new FullAlbumEntity(artsitName, albumName, 0, url, lastfmId);
                    fullAlbumEntity.setTrackList(tracks);
                }
                Track e = new Track(artsitName, trackName, plays, loved, duration);
                e.setPosition(position);
                tracks.add(e);
            }
            return Optional.ofNullable(fullAlbumEntity);
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<ScrobbledTrack> getUserTopTracks(Connection connection, String lastfmid) {
        List<ScrobbledTrack> scrobbledTracks = new ArrayList<>();

        String mySql = "SELECT b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),a.playnumber,a.loved " +
                       "FROM scrobbled_track a JOIN track b ON a.track_id = b.id JOIN artist c ON b.artist_id = c.id LEFT JOIN album d ON b.album_id = d.id WHERE a.lastfm_id = ? ORDER BY playnumber DESC";
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

        String mySql = "SELECT b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),a.playnumber,a.loved,b.spotify_id " +
                       "FROM scrobbled_track a JOIN track b ON a.track_id = b.id JOIN artist c ON b.artist_id = c.id LEFT JOIN album d ON b.album_id = d.id WHERE a.lastfm_id = ? AND b.spotify_id IS NOT NULL ORDER BY playnumber DESC LIMIT ? ";
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
    public void storeTrackList(Connection connection, long albumId, List<ScrobbledTrack> trackList) {

        StringBuilder mySql =
                new StringBuilder("INSERT ignore INTO album_tracklist (album_id,track_id,position) VALUES (?,?,?)");

        mySql.append(",(?,?,?)".repeat(Math.max(0, trackList.size() - 1)));

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            for (int i = 0; i < trackList.size(); i++) {
                ScrobbledTrack x = trackList.get(i);
                preparedStatement.setLong(3 * i + 1, x.getAlbumId());
                preparedStatement.setLong(3 * i + 2, x.getTrackId());
                preparedStatement.setInt(3 * i + 3, x.getPosition());
            }
            preparedStatement.execute();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Optional<FullAlbumEntity> getServerAlbumTrackList(Connection connection, long albumId, long guildId) {
        List<Track> tracks = new ArrayList<>();
        FullAlbumEntity fullAlbumEntity = null;


        String mySql =
                """
                        SELECT d.name,\s
                        b.album_name,\s
                        c.duration,\s
                        c.track_name,\s
                        COALESCE(c.url, b.url, d.url),\s
                        coalesce(sum(main.playnumber),0),
                        a.position\s
                        FROM   album_tracklist a\s
                        LEFT JOIN album b\s
                        ON a.album_id = b.id\s
                        LEFT JOIN track c\s
                        ON a.track_id = c.id\s
                        LEFT JOIN artist d\s
                        ON c.artist_id = d.id								 \s
                         LEFT JOIN  ( SELECT track_id,playnumber FROM scrobbled_track e JOIN user f ON e.lastfm_id = f.lastfm_id JOIN user_guild g ON f.discord_id = g.discord_id AND g.guild_id = ?) main  ON main.track_id = a.track_id                                     \s
                        WHERE  a.album_id = ?  GROUP BY a.track_id
                        ORDER  BY a.position ASC\s
                                                
                         """;

        try
                (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setLong(1, guildId);
            preparedStatement.setLong(2, albumId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                String artsitName = resultSet.getString(1);
                String albumName = resultSet.getString(2);
                int duration = resultSet.getInt(3);
                String trackName = resultSet.getString(4);
                String url = resultSet.getString(5);
                int plays = resultSet.getInt(6);
                int position = resultSet.getInt(7);

                if (fullAlbumEntity == null) {
                    fullAlbumEntity = new FullAlbumEntity(artsitName, albumName, 0, url, null);
                    fullAlbumEntity.setTrackList(tracks);
                }
                Track e = new Track(artsitName, trackName, plays, false, duration);
                e.setPosition(position);
                tracks.add(e);
            }
            return Optional.ofNullable(fullAlbumEntity);
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Optional<FullAlbumEntity> getGlobalAlbumTrackList(Connection connection, long albumId) {
        List<Track> tracks = new ArrayList<>();
        FullAlbumEntity fullAlbumEntity = null;


        String mySql =
                """
                        SELECT d.name,\s
                                                        b.album_name,\s
                                                        c.duration,\s
                                                        c.track_name,\s
                                                        COALESCE(c.url, b.url, d.url),\s
                                                        coalesce(sum(e.playnumber),0),
                                                        a.position\s
                                                 FROM   album_tracklist a\s
                                                        JOIN album b\s
                                                          ON a.album_id = b.id\s
                                                        JOIN track c\s
                                                          ON a.track_id = c.id\s
                                                        JOIN artist d\s
                                                          ON c.artist_id = d.id\s
                                                        LEFT JOIN scrobbled_track e\s
                                                               ON a.track_id = e.track_id\s
                                                 WHERE  a.album_id = ?\s
                                                 GROUP BY a.track_id
                                                 ORDER  BY a.position ASC\s
                         """;

        try
                (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setLong(1, albumId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                String artsitName = resultSet.getString(1);
                String albumName = resultSet.getString(2);
                int duration = resultSet.getInt(3);
                String trackName = resultSet.getString(4);
                String url = resultSet.getString(5);
                int plays = resultSet.getInt(6);
                int position = resultSet.getInt(7);

                if (fullAlbumEntity == null) {
                    fullAlbumEntity = new FullAlbumEntity(artsitName, albumName, 0, url, null);
                    fullAlbumEntity.setTrackList(tracks);
                }
                Track e = new Track(artsitName, trackName, plays, false, duration);
                e.setPosition(position);
                tracks.add(e);
            }
            return Optional.ofNullable(fullAlbumEntity);
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public ResultWrapper<ScrobbledTrack> getGuildTopTracks(Connection connection, Long guildID, int limit, boolean doCount) {


        String normalQUery = "SELECT d.name,f.track_name,coalesce(f.url,e.url,d.url) as url,sum(playnumber) AS orden ,e.mbid  ";

        String countQuery = "Select count(*) as orden ";


        String queryBody = "FROM  scrobbled_track a use index (scrobbled_track_fk_user)" +
                           " JOIN user b" +
                           " ON a.lastfm_id = b.lastfm_id" +
                           " JOIN artist d " +
                           " ON a.artist_id = d.id" +
                           " join track f on a.track_id = f.id " +
                           " left join album e on f.album_id = e.id ";

        if (guildID != null) {
            queryBody += " JOIN  user_guild c" +
                         " ON b.discord_id=c.discord_id" +
                         " WHERE c.guild_id = ?";
        }

        List<ScrobbledTrack> list = new ArrayList<>();
        int count = 0;
        int i = 1;
        try (PreparedStatement preparedStatement1 = connection.prepareStatement(normalQUery + queryBody + " GROUP BY track_id,url  ORDER BY orden DESC  LIMIT ?")) {
            if (guildID != null)
                preparedStatement1.setLong(i++, guildID);

            preparedStatement1.setInt(i, limit);

            ResultSet resultSet1 = preparedStatement1.executeQuery();

            while (resultSet1.next()) {
                String artist = resultSet1.getString("d.name");
                String album = resultSet1.getString("f.track_name");
                String mbid = resultSet1.getString("e.mbid");


                String url = resultSet1.getString("url");

                int plays = resultSet1.getInt("orden");
                ScrobbledTrack who = new ScrobbledTrack(artist, album, plays, false, 0, url, null, null);
                who.setCount(plays);
                list.add(who);
            }
            if (doCount) {

                PreparedStatement preparedStatement = connection.prepareStatement(countQuery + queryBody);
                i = 1;
                if (guildID != null)
                    preparedStatement.setLong(i, guildID);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }


            }
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return new ResultWrapper<>(count, list);
    }

    @Override
    public Set<String> getTrackTags(Connection connection, Long trackId) {
        String queryString = "SELECT tag FROM track_tags WHERE track_id = ?";
        Set<String> returnList = new HashSet<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, trackId);
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

    @Override
    public List<ScrobbledTrack> getUserTopTracksNoSpotifyId(Connection connection, String lastfmid, int limit) {
        List<ScrobbledTrack> scrobbledTracks = new ArrayList<>();

        String mySql = "SELECT b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),a.playnumber,a.loved " +
                       "FROM scrobbled_track a JOIN track b ON a.track_id = b.id JOIN artist c ON b.artist_id = c.id LEFT JOIN album d ON b.album_id = d.id WHERE a.lastfm_id = ? AND b.spotify_id IS NULL ORDER BY playnumber DESC LIMIT ? ";
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
        String queryString = "DELETE   FROM scrobbled_track  WHERE lastfm_id = ? ";
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
                new StringBuilder("INSERT INTO track (artist_id,track_name,url,mbid,duration,album_id) VALUES (?,?,?,?,?,?)");

        mySql.append(",(?,?,?,?,?,?)".repeat(Math.max(0, nonExistingId.size() - 1)));
        mySql.append(" on duplicate key update " +
                     " mbid = if(mbid is null and values(mbid) is not null,values(mbid),mbid), " +
                     " duration = if(duration is null and values(duration) is not null,values(duration),duration), " +
                     " url = if(url is null and values(url) is not null,values(url),url), " +
                     " album_id = if(album_id is null and values(album_id) is not null,values(album_id),album_id)  returning id ");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            for (int i = 0; i < nonExistingId.size(); i++) {
                ScrobbledTrack x = nonExistingId.get(i);
                preparedStatement.setLong(6 * i + 1, x.getArtistId());
                preparedStatement.setString(6 * i + 2, x.getName());
                preparedStatement.setString(6 * i + 3, x.getUrl());
                String trackMbid = x.getMbid();
                if (trackMbid == null || trackMbid.isBlank()) {
                    trackMbid = null;
                }
                preparedStatement.setString(6 * i + 4, trackMbid);
                preparedStatement.setInt(6 * i + 5, x.getDuration());
                if (x.getAlbumId() < 1) {
                    preparedStatement.setNull(6 * i + 6, Types.BIGINT);
                } else {
                    preparedStatement.setLong(6 * i + 6, x.getAlbumId());
                }


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
        String sql = "INSERT INTO track (artist_id,track_name,url,mbid,duration,album_id) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE" +
                     " mbid = if(mbid IS NULL AND values(mbid) IS NOT NULL,values(mbid),mbid), " +
                     " duration = if(duration IS NULL AND values(duration) IS NOT NULL,values(duration),duration), " +
                     " url = if(url IS NULL AND values(url) IS NOT NULL,values(url),url), " +
                     " album_id = if(album_id IS NULL AND values(album_id) IS NOT NULL,values(album_id),album_id)  RETURNING id ";

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
            if (x.getAlbumId() < 1) {
                preparedStatement.setNull(6, Types.BIGINT);
            } else {
                preparedStatement.setLong(6, x.getAlbumId());
            }

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
        String queryString = "SELECT id FROM  track WHERE track_name = ? AND artist_id = ?  ";
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
    public ScrobbledTrack getTrackByName(Connection connection, String track, long artistId) {
//         String queryString = "SELECT id,artist_id,album_name,url,duration,mbid,spotify_id FROM  album WHERE album_name = ? and artist_id = ?  ";
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
    public List<Track> getUserTopArtistTracks(Connection connection, String lastfmId, long artistId, int limit) {
        List<Track> returnList = new ArrayList<>();
        String mySql = "SELECT a.playnumber,b.track_name,d.name,b.url,a.loved,duration FROM scrobbled_track a JOIN track b ON a.track_id = b.id " +
                       "JOIN artist d ON b.artist_id = d.id  WHERE a.lastfm_id = ? AND a.artist_id = ?  ORDER BY a.playnumber DESC  LIMIT ? ";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setString(i++, lastfmId);
            preparedStatement.setLong(i++, artistId);
            preparedStatement.setInt(i, limit);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int plays = resultSet.getInt(1);
                String trackName = resultSet.getString(2);
                String artist = resultSet.getString(3);
                boolean isLoved = resultSet.getBoolean(5);
                int duration = resultSet.getInt(6);

                Track e = new Track(artist, trackName, plays, isLoved, duration == 0 ? 200 : duration);
                returnList.add(e);
            }

        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public Map<ScrobbledArtist, Long> getUserTopArtistByDuration(Connection connection, String lastfmId, int limit) {
        Map<ScrobbledArtist, Long> returnList = new LinkedHashMap<>();
        String mySql = "SELECT sum(playnumber) AS plays,sum(coalesce(duration,200)) AS ord,d.name,d.url,d.id FROM scrobbled_track a JOIN track b ON a.track_id = b.id JOIN user c ON a.lastfm_id = c.lastfm_id" +
                       " JOIN artist d ON b.artist_id = d.id  WHERE a.lastfm_id = ?  " +
                       " GROUP BY a.artist_id  " +
                       "ORDER BY ord DESC  LIMIT ? ";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setString(i++, lastfmId);
            preparedStatement.setInt(i, limit);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int plays = resultSet.getInt(1);
                long seconds = resultSet.getLong(2);
                String artist = resultSet.getString(3);
                String url = resultSet.getString(4);
                long id = resultSet.getLong(5);
                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, plays, url);
                scrobbledArtist.setArtistId(id);
                returnList.put(scrobbledArtist, seconds);
            }

        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<AlbumUserPlays> getServerTopArtistTracks(Connection connection, long guildId, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = "SELECT sum(a.playnumber) AS ord,b.track_name,d.name,b.url FROM scrobbled_track a JOIN track b ON a.track_id = b.id JOIN user c ON a.lastfm_id = c.lastfm_id" +
                       " JOIN user_guild e ON c.discord_id = e.discord_id " +
                       "JOIN artist d ON b.artist_id = d.id  WHERE e.guild_id = ? AND a.artist_id = ?  " +
                       " GROUP BY a.track_id " +
                       "ORDER BY ord DESC  LIMIT ? ";
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
        String mySql = "SELECT sum(a.playnumber) AS ord,b.track_name,d.name,b.url FROM scrobbled_track a JOIN track b ON a.track_id = b.id" +
                       " JOIN artist d ON b.artist_id = d.id  WHERE  a.artist_id = ?" +
                       " GROUP BY a.track_id " +
                       "  ORDER BY ord DESC  LIMIT ? ";
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
//         String queryString = "SELECT tag,count(*) as coun FROM album a join artist b on a.artist_id =b.id join album_tags c  on a.id = c.album_id WHERE (name) in (%s) and (album_name)  IN (%s) group by tag";
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
    public String getTrackUrlByName(Connection connection, String name, long artistId) {
//         String queryString = "SELECT url FROM  album WHERE album_name = ? and artist_id = ?  ";
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
