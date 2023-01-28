package dao;

import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.SQLUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public void fillIdsMbids(Connection connection, List<ScrobbledTrack> list) {
        if (list.isEmpty()) {
            return;
        }
        String queryString = "SELECT id,mbid FROM track WHERE  mbid in (%s)  ";

        String sql = String.format(queryString, prepareINQuerySingle(list.size()));

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setString(i + 1, list.get(i).getMbid());
            }

            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();
//            Map<String, ScrobbledTrack> secundaryMap = new HashMap<>();
            Map<String, ScrobbledTrack> trackMap = list.stream().collect(Collectors.toMap(ScrobbledTrack::getMbid, Function.identity(), (scrobbledArtist, scrobbledArtist2) -> scrobbledArtist));
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

        String mySql = "SELECT d.name,b.album_name,c.duration,c.track_name,coalesce(c.url,b.url,d.url),coalesce(e.playnumber,0),coalesce(e.loved,FALSE),a.position " + "FROM album_tracklist a JOIN album b ON a.album_id =b.id JOIN track c ON a.track_id = c.id JOIN artist d ON c.artist_id = d.id" + " LEFT JOIN (SELECT * FROM scrobbled_track WHERE lastfm_id = ? ) e ON a.track_id = e.track_id  WHERE a.album_id = ?   ORDER BY a.position ASC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
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
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public ScrobbledTrack getUserTrackInfo(Connection connection, String lastfmid, long trackId) {
        List<ScrobbledTrack> scrobbledTracks = new ArrayList<>();

        String mySql = "SELECT b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),a.playnumber,a.loved,b.popularity " + "FROM scrobbled_track a JOIN track b ON a.track_id = b.id  JOIN artist c ON b.artist_id = c.id LEFT JOIN album d ON b.album_id = d.id WHERE a.lastfm_id = ?  AND b.id = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setString(1, lastfmid);
            preparedStatement.setLong(2, trackId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                long albumId = resultSet.getLong(2);
                long artistId = resultSet.getLong(3);
                String artsitName = resultSet.getString(4);
                String albumName = resultSet.getString(5);
                int duration = resultSet.getInt(6);
                String trackName = resultSet.getString(7);
                String url = resultSet.getString(8);
                int plays = resultSet.getInt(9);
                boolean loved = resultSet.getBoolean(10);
                int pop = resultSet.getInt(11);

                ScrobbledTrack e = new ScrobbledTrack(artsitName, trackName, plays, loved, duration, url, null, null);
                e.setArtistId(artistId);
                e.setAlbumId(albumId);
                e.setTrackId(trackId);
                e.setPopularity(pop);
                return e;
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return null;
    }


    @Override
    public List<ScrobbledTrack> getUserTopTracks(Connection connection, String lastfmid, Integer limit) {
        List<ScrobbledTrack> scrobbledTracks = new ArrayList<>();
        String mySql = """
                select b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),playnumber,popularity
                from track b
                join
                (
                    select track_id,playnumber
                    from scrobbled_track a
                    where lastfm_id = ?
                    order by playnumber
                ) main
                on b.id = main.track_id
                join artist c on b.artist_id = c.id
                left join album d on d.id = b.album_id
                order by playnumber desc
                """;
        if (limit != null) {
            mySql += " limit ?";
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setString(1, lastfmid);
            if (limit != null) {
                preparedStatement.setInt(2, limit);
            }
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
                int pop = resultSet.getInt(10);

                ScrobbledTrack e = new ScrobbledTrack(artsitName, trackName, plays, false, duration, url, null, null);
                e.setArtistId(artistId);
                e.setAlbumId(albumId);
                e.setTrackId(trackId);
                e.setPopularity(pop);
                scrobbledTracks.add(e);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return scrobbledTracks;
    }

    @Override
    public List<ScrobbledTrack> getTopSpotifyTracksIds(Connection connection, String lastfmId, int limit) {
        List<ScrobbledTrack> scrobbledTracks = new ArrayList<>();

        String mySql = "SELECT b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),a.playnumber,a.loved,b.spotify_id " + "FROM scrobbled_track a JOIN track b ON a.track_id = b.id JOIN artist c ON b.artist_id = c.id LEFT JOIN album d ON b.album_id = d.id WHERE a.lastfm_id = ? AND b.spotify_id IS NOT NULL ORDER BY playnumber DESC LIMIT ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
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
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return scrobbledTracks;
    }


    @Override
    public void storeTrackList(Connection connection, long albumId, List<ScrobbledTrack> trackList) {

        StringBuilder mySql = new StringBuilder("INSERT ignore INTO album_tracklist (album_id,track_id,position) VALUES (?,?,?)");

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


        String mySql = """
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
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
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Optional<FullAlbumEntity> getGlobalAlbumTrackList(Connection connection, long albumId) {
        List<Track> tracks = new ArrayList<>();
        FullAlbumEntity fullAlbumEntity = null;


        String mySql = """
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
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
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public ResultWrapper<ScrobbledTrack> getGuildTopTracks(Connection connection, Long guildID, int limit, boolean doCount) {

        String innerSelect = """
                (select track_id,playnumber
                from scrobbled_track a
                """;
        if (guildID != null) {
            innerSelect += """
                    join user b on a.lastfm_id = b.lastfm_id
                    join user_guild c on b.discord_id = c.discord_id
                    where c.guild_id = ?""";
        }
        innerSelect += ") main ";

        String normalQUery = """
                SELECT
                (SELECT name FROM artist a JOIN track b ON a.id = b.artist_id WHERE b.id = result.track_id) AS name,
                (SELECT track_name FROM track WHERE id = result.track_id) AS track_name,
                (SELECT coalesce(f.url,e.url,d.url) FROM track f JOIN artist d ON f.artist_id = d.id LEFT JOIN album e ON f.album_id = e.id WHERE f.id = result.track_id) AS url
                ,orden AS orden
                FROM (SELECT sum(playnumber) AS orden,track_id FROM ${main} GROUP BY track_id ORDER BY  orden DESC LIMIT ? ) result
                """.replace("${main}", innerSelect);

        String countQuery = "SELECT count(*) AS orden FROM " + innerSelect;


        List<ScrobbledTrack> list = new ArrayList<>();
        int count = 0;
        int i = 1;
        try (PreparedStatement preparedStatement1 = connection.prepareStatement(normalQUery)) {
            if (guildID != null) preparedStatement1.setLong(i++, guildID);

            preparedStatement1.setInt(i, limit);

            ResultSet resultSet1 = preparedStatement1.executeQuery();

            while (resultSet1.next()) {
                String artist = resultSet1.getString("name");
                String album = resultSet1.getString("track_name");


                String url = resultSet1.getString("url");

                int plays = resultSet1.getInt("orden");
                ScrobbledTrack who = new ScrobbledTrack(artist, album, plays, false, 0, url, null, null);
                who.setCount(plays);
                list.add(who);
            }
            if (doCount) {

                PreparedStatement preparedStatement = connection.prepareStatement(countQuery);
                i = 1;
                if (guildID != null) preparedStatement.setLong(i, guildID);

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
    public void updateLovedSongs(Connection connection, Set<Long> ids, boolean loved, String lastfmId) {
        if (ids.isEmpty()) {
            return;
        }
        String sql = "UPDATE scrobbled_track set loved = ? where lastfm_id = ? and track_id in (" + prepareINQuerySingle(ids.size()) + ")";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int i = 1;
            preparedStatement.setBoolean(i++, loved);
            preparedStatement.setString(i++, lastfmId);
            for (Long id : ids) {
                preparedStatement.setLong(i++, id);
            }
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void resetLovedSongs(Connection connection, String lastfm) {
        String sql = "UPDATE scrobbled_track SET loved = FALSE WHERE lastfm_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, lastfm);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Pair<Long, Track> findTrackByName(Connection connection, String track, long artistId) throws InstanceNotFoundException {
        String queryString = "SELECT id,track_name,url FROM  track WHERE track_name = ? AND artist_id = ?  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(2, artistId);
            preparedStatement.setString(1, track);
            ResultSet execute = preparedStatement.executeQuery();
            if (execute.next()) {
                long id = execute.getLong(1);
                String name = execute.getString(2);
                String url = execute.getString(3);
                Track trackR = new Track(null, name, 0, false, 0);
                trackR.setImageUrl(url);
                return Pair.of(id, trackR);
            }
            throw new InstanceNotFoundException(track);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteTracklist(Connection connection, long albumId) {
        String query = "delete from album_tracklist where album_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, albumId);
            preparedStatement.executeUpdate();
        } catch (SQLException sql) {
            throw new ChuuServiceException(sql);
        }
    }

    @Override
    public List<ScrobbledTrack> getUserTopTracksNoSpotifyId(Connection connection, String lastfmid, int limit) {
        List<ScrobbledTrack> scrobbledTracks = new ArrayList<>();

        String mySql = "SELECT b.id,d.id,c.id,c.name,d.album_name,b.duration,b.track_name,coalesce(b.url,d.url,c.url),a.playnumber,a.loved " + "FROM scrobbled_track a JOIN track b ON a.track_id = b.id JOIN artist c ON b.artist_id = c.id LEFT JOIN album d ON b.album_id = d.id WHERE a.lastfm_id = ? AND b.spotify_id IS NULL ORDER BY playnumber DESC LIMIT ? ";
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
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return scrobbledTracks;
    }


    @Override
    public void fillIds(Connection connection, List<ScrobbledTrack> list) {
        String seed = UUID.randomUUID().toString();

        Map<String, ScrobbledTrack> trackMap = list.stream().collect(Collectors.toMap(scrobbledTrack -> scrobbledTrack.getArtistId() + "_" + seed + "_" + scrobbledTrack.getName().toLowerCase(), Function.identity(), (scrobbledArtist, scrobbledArtist2) -> {
            scrobbledArtist.setCount(scrobbledArtist.getCount() + scrobbledArtist2.getCount());
            return scrobbledArtist;
        }));
        Pattern compile = Pattern.compile("\\p{M}");
        SQLUtils.doBatchesSelect(connection, "SELECT id,artist_id,track_name FROM track USE INDEX (track_and_artist) WHERE  (artist_id,track_name) in  ( ", list, (ps, st, i) -> {
            ps.setLong(2 * i + 1, st.getArtistId());
            ps.setString(2 * i + 2, st.getName());
        }, rs -> {
            long id = rs.getLong("id");
            long artist_id = rs.getLong("artist_id");

            String name = rs.getString("track_name");
            ScrobbledTrack scrobbledTrack = trackMap.get(artist_id + "_" + seed + "_" + name.toLowerCase());
            if (scrobbledTrack != null) {
                scrobbledTrack.setTrackId(id);
            } else {
                // name can be stripped or maybe the element is collect is the stripped one
                String normalizeArtistName = compile.matcher(Normalizer.normalize(name, Normalizer.Form.NFKD)).replaceAll("");
                ScrobbledTrack normalizedArtist = trackMap.get(artist_id + "_" + seed + "_" + normalizeArtistName.toLowerCase());
                if (normalizedArtist != null) {
                    normalizedArtist.setTrackId(id);
                }
            }
        }, 2, " )");

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
        StringBuilder mySql = new StringBuilder("INSERT INTO track (artist_id,track_name,url,mbid,duration,album_id,spotify_id) VALUES (?,?,?,?,?,?,?)");

        mySql.append(",(?,?,?,?,?,?,?)".repeat(Math.max(0, nonExistingId.size() - 1)));
        mySql.append(" on duplicate key update  " +
                "mbid = if(mbid is null and values(mbid) is not null,values(mbid),mbid), " +
                " duration = if(duration is null and values(duration) is not null and values(duration) != 0,values(duration),duration)," +
                "url = if(url is null and values(url) is not null,values(url),url)," +
                "album_id = if(album_id is null and values(album_id) is not null,values(album_id),album_id),  " +
                "spotify_id = if(spotify_id is null and values(spotify_id) is not null,values(spotify_id),spotify_id)  " +
                "returning id ");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            for (int i = 0; i < nonExistingId.size(); i++) {
                ScrobbledTrack x = nonExistingId.get(i);
                preparedStatement.setLong(7 * i + 1, x.getArtistId());
                preparedStatement.setString(7 * i + 2, x.getName());
                preparedStatement.setString(7 * i + 3, x.getUrl());
                String trackMbid = x.getMbid();
                if (trackMbid == null || trackMbid.isBlank()) {
                    trackMbid = null;
                }
                preparedStatement.setString(7 * i + 4, trackMbid);
                preparedStatement.setInt(7 * i + 5, x.getDuration());
                if (x.getAlbumId() < 1) {
                    preparedStatement.setNull(7 * i + 6, Types.BIGINT);
                } else {
                    preparedStatement.setLong(7 * i + 6, x.getAlbumId());
                }
                preparedStatement.setString(7 * i + 7, x.getSpotifyId());


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
        String sql = "INSERT INTO track (artist_id,track_name,url,mbid,duration,album_id) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE" + " mbid = if(mbid IS NULL AND values(mbid) IS NOT NULL,values(mbid),mbid), " + " duration = if(duration IS NULL AND values(duration) IS NOT NULL,values(duration),duration), " + " url = if(url IS NULL AND values(url) IS NOT NULL,values(url),url), " + " album_id = if(album_id IS NULL AND values(album_id) IS NOT NULL,values(album_id),album_id)  RETURNING id ";

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
        } catch (SQLException e) {
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
    public void addSrobbledTracks(Connection con, List<ScrobbledTrack> scrobbledTracks) {

        SQLUtils.doBatches(con, "INSERT INTO scrobbled_track(artist_id,track_id,lastfm_id,playnumber,loved) VALUES ", scrobbledTracks, (ps, st, i) -> {
            ps.setLong(5 * i + 1, st.getArtistId());
            ps.setLong(5 * i + 2, st.getTrackId());
            ps.setString(5 * i + 3, st.getDiscordID());
            ps.setInt(5 * i + 4, st.getCount());
            ps.setBoolean(5 * i + 5, st.isLoved());
        }, 5, " ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber, loved = VALUES(loved) ");


//        StringBuilder mySql =
//                new StringBuilder("INSERT INTO  scrobbled_track" +
//                                  "                  (artist_id,track_id,lastfm_id,playnumber,loved) VALUES (?,?,?,?,?) ");
//
//        mySql.append(", (?,?,?,?,?)".repeat(Math.max(0, scrobbledTracks.size() - 1)));
//        mySql.append(" ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber, loved = VALUES(loved)");
//
//        try {
//            PreparedStatement preparedStatement = con.prepareStatement(mySql.toString());
//            for (int i = 0; i < scrobbledTracks.size(); i++) {
//                ScrobbledTrack scrobbledTrack = scrobbledTracks.get(i);
//                preparedStatement.setLong(5 * i + 1, scrobbledTrack.getArtistId());
//                preparedStatement.setLong(5 * i + 2, scrobbledTrack.getTrackId());
//                preparedStatement.setString(5 * i + 3, scrobbledTrack.getDiscordID());
//                preparedStatement.setInt(5 * i + 4, scrobbledTrack.getCount());
//                preparedStatement.setBoolean(5 * i + 5, scrobbledTrack.isLoved());
//            }
//            preparedStatement.execute();
//        } catch (SQLException e) {
//            throw new ChuuServiceException(e);
//        }
    }


    @Override
    public List<Track> getUserTopArtistTracksDuration(Connection connection, String lastfmId, long artistId, int limit) {
        List<Track> returnList = new ArrayList<>();
        String mySql = """
                SELECT
                    a.playnumber,
                    (SELECT track_name FROM track WHERE id = a.track_id),
                    (SELECT coalesce(nullif(duration,0),200) FROM track WHERE id = a.track_id) AS duration,
                    (SELECT url FROM track WHERE id = a.track_id)
                FROM scrobbled_track a
                WHERE
                    a.lastfm_id = ?
                    AND a.artist_id = ?
                ORDER BY a.playnumber * duration DESC
                LIMIT ?""";
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
                int duration = resultSet.getInt(3);
                String url = resultSet.getString(4);

                Track e = new Track(null, trackName, plays, false, duration == 0 ? 200 : duration);
                e.setImageUrl(url);
                returnList.add(e);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
    }


    @Override
    public List<Track> getUserTopArtistTracks(Connection connection, String lastfmId, long artistId, int limit) {
        List<Track> returnList = new ArrayList<>();
        String mySql = """
                SELECT
                    a.playnumber,
                    (SELECT track_name FROM track WHERE id = a.track_id),
                    (SELECT url FROM track WHERE id = a.track_id)
                FROM scrobbled_track a
                WHERE
                    a.lastfm_id = ?
                    AND a.artist_id = ?
                ORDER BY a.playnumber DESC
                LIMIT ?""";
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
                String url = resultSet.getString(3);

                Track e = new Track(null, trackName, plays, false, 0);
                e.setImageUrl(url);
                returnList.add(e);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public Map<ScrobbledArtist, Long> getUserTopArtistByDuration(Connection connection, String lastfmId, int limit) {
        Map<ScrobbledArtist, Long> returnList = new LinkedHashMap<>();
        String mySql = "SELECT sum(playnumber) AS plays,sum(coalesce(duration,200)) AS ord,d.name,d.url,d.id FROM scrobbled_track a JOIN track b ON a.track_id = b.id JOIN user c ON a.lastfm_id = c.lastfm_id" + " JOIN artist d ON b.artist_id = d.id  WHERE a.lastfm_id = ?  " + " GROUP BY a.artist_id  " + "ORDER BY ord DESC  LIMIT ? ";
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

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
    }

    @Override
    public List<AlbumUserPlays> getServerTopArtistTracks(Connection connection, long guildId, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = """
                SELECT
                    sum(playnumber) AS ord,
                    (SELECT track_name FROM track WHERE id = a.id),
                    (SELECT url FROM track WHERE id = a.id)
                FROM
                        (SELECT id FROM track WHERE artist_id = ?) a
                            JOIN scrobbled_track b ON a.id = b.track_id
                            JOIN user c ON b.lastfm_id = c.lastfm_id
                            JOIN user_guild e ON c.discord_id = e.discord_id
                WHERE
                    e.guild_id = ?
                GROUP BY
                    a.id
                ORDER BY
                    ord DESC
                LIMIT ?
                """;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setLong(i++, artistId);
            preparedStatement.setLong(i++, guildId);
            processArtistTracks(limit, returnList, preparedStatement, i);

        } catch (SQLException e) {
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
            String url = resultSet.getString(3);
            AlbumUserPlays e = new AlbumUserPlays(trackName, url);
            e.setPlays(plays);
            returnList.add(e);
        }
    }

    @Override
    public List<AlbumUserPlays> getGlobalTopArtistTracks(Connection connection, long artistId, int limit) {
        List<AlbumUserPlays> returnList = new ArrayList<>();
        String mySql = """
                SELECT
                    sum(playnumber) AS ord,
                    (SELECT track_name FROM track WHERE id = a.id),
                    (SELECT url FROM track WHERE id = a.id)
                FROM
                        (SELECT id FROM track WHERE artist_id = ?) a
                            JOIN scrobbled_track b ON a.id = b.track_id
                GROUP BY
                    a.id
                ORDER BY
                    ord DESC
                LIMIT ?;
                """;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setLong(i++, artistId);
            processArtistTracks(limit, returnList, preparedStatement, i);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
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

    @Override
    public List<UnheardCount> getUnheardSongs(Connection connection, String lastFmName, long artistId, boolean listeners, Long filter) {
        List<UnheardCount> unheard = new ArrayList<>();

        String mySql = """
                with ids as (select *
                             from (Select id
                                   from track a
                                   where artist_id = ?
                                
                                   EXCEPT
                                   Select a.track_id
                                   from scrobbled_track a
                                   where artist_id = ?
                                     and lastfm_id = ?) main)
                select track_name, count(*) listeners, sum(a.playnumber) as plays
                from scrobbled_track a
                         join track t on t.id = a.track_id
                where track_id in (select id from ids)
                """;


        mySql += " group by t.id, track_name ";
        if (filter != null) {
            mySql += "having plays > ? ";
        }
        if (listeners) {
            mySql += "order by listeners desc";
        } else {
            mySql += "order by plays desc ";
        }


        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setLong(1, artistId);
            preparedStatement.setLong(2, artistId);
            preparedStatement.setString(3, lastFmName);
            if (filter != null) {
                preparedStatement.setLong(4, filter);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String trackName = resultSet.getString(1);
                long listenrs = resultSet.getLong(2);
                long scrobbles = resultSet.getLong(3);
                unheard.add(new UnheardCount(trackName, listenrs, scrobbles));
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return unheard;
    }

    @Override
    public List<Track> getUserTracksByLength(Connection connection, String lastfmId, boolean longestFirst, Integer limit) {
        List<Track> returnList = new ArrayList<>();
        String mySql = """
                SELECT
                    a.playnumber,
                    b.track_name,
                    duration,
                    b.url,
                    c.name
                FROM scrobbled_track a
                join track b on a.track_id = b.id
                join artist c on b.artist_id = c.id
                WHERE
                    a.lastfm_id = ?
                    and duration != 0
                ORDER BY duration
                """;
        if (longestFirst) {
            mySql += " desc";
        }
        if (limit != null) {
            mySql += " LIMIT ?";
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql);
            int i = 1;
            preparedStatement.setString(i++, lastfmId);
            if (limit != null) {
                preparedStatement.setInt(i, limit);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int plays = resultSet.getInt(1);
                String trackName = resultSet.getString(2);
                int duration = resultSet.getInt(3);
                String url = resultSet.getString(4);
                String name = resultSet.getString(5);

                Track e = new Track(name, trackName, plays, false, duration == 0 ? 200 : duration);
                e.setImageUrl(url);
                returnList.add(e);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnList;
    }
}
