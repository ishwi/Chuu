package dao;

import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SQLRYMDaoImpl implements SQLRYMDao {
    @Override
    public void setServerTempTable(Connection connection, List<RYMImportRating> ratings) {

        String queryBody =
                """
                                CREATE TEMPORARY TABLE temp_rating(
                                        rym_id bigint(20) PRIMARY KEY,
                                        last_name varchar(400) COLLATE  utf8mb4_unicode_ci ,
                                        first_name varchar(20) COLLATE  utf8mb4_unicode_ci,
                                        first_localized_name varchar(20) COLLATE  utf8mb4_unicode_ci,
                                        last_localized_name varchar(400) COLLATE  utf8mb4_unicode_ci
                        ) DEFAULT CHARSET=utf8mb4 COLLATE =  utf8mb4_general_ci;""".indent(24);

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();

            queryBody =
                    "                                insert into temp_rating(rym_id,last_name,first_name,first_localized_name,last_localized_name) values %s";
            String sql = String.format(queryBody, ratings.isEmpty() ? (null) : String.join(",", Collections.nCopies(ratings.size(), "(?,?,?,?,?)")));
            try (PreparedStatement preparedStatement2 = connection.prepareStatement(sql)) {

                int i = 1;
                for (RYMImportRating rating : ratings) {
                    preparedStatement2.setLong(i++, rating.getRYMid());
                    preparedStatement2.setString(i++, rating.getLastName());
                    preparedStatement2.setString(i++, rating.getFirstName());
                    preparedStatement2.setString(i++, rating.getFirstNameLocalized());
                    preparedStatement2.setString(i++, rating.getLastNameLocalized());
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
    public Map<Long, Long> findArtists(Connection connection) {
        HashMap<Long, Long> returnedMap = new HashMap<>();
        String s = "Select b.id,a.rym_id from temp_rating a left join artist b " +
                "on a.last_name = name  " +
                "or if(concat(a.first_name,' ',a.last_name) = '',null,concat(a.first_localized_name,' ',a.last_localized_name))= name " +
                "or  if(concat(a.first_localized_name,' ',a.last_localized_name) = '',null,concat(a.first_localized_name,' ',a.last_localized_name)) = name " +
                "or if(last_localized_name = '',null,last_localized_name) = name ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long artist_id = resultSet.getLong(1);
                long rymId = resultSet.getLong(2);
                returnedMap.put(rymId, artist_id);

            }
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
        return returnedMap;
    }

    @Override
    public Map<Long, Long> findArtistsByLocalizedJoinedNames(Connection connection) {
        HashMap<Long, Long> returnedMap = new HashMap<>();
        String s = "Select b.id,a.rym_id from temp_rating a left join artist b " +
                " on  " +
                " concat(a.first_localized_name,' ',a.last_localized_name)= name ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long artist_id = resultSet.getLong(1);
                long rymId = resultSet.getLong(2);
                returnedMap.put(rymId, artist_id);

            }
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
        return returnedMap;
    }

    @Override
    public Map<Long, Long> findArtistsByLocalizedNames(Connection connection) {
        HashMap<Long, Long> returnedMap = new HashMap<>();
        String s = "Select b.id,a.rym_id from temp_rating a left join artist b " +
                " on  " +
                "a.last_localized_name = name ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long artist_id = resultSet.getLong(1);
                long rymId = resultSet.getLong(2);
                returnedMap.put(rymId, artist_id);

            }
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
        return returnedMap;
    }

    @Override
    public Map<Long, Long> findArtistsByJoinedNames(Connection connection) {
        HashMap<Long, Long> returnedMap = new HashMap<>();
        String s = "Select b.id,a.rym_id from temp_rating a left join artist b " +
                " on  " +
                "a.last_name = name ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long artist_id = resultSet.getLong(1);
                long rymId = resultSet.getLong(2);
                returnedMap.put(rymId, artist_id);

            }
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
        return returnedMap;
    }

    @Override
    public Map<Long, Long> findArtistsByNames(Connection connection) {
        HashMap<Long, Long> returnedMap = new HashMap<>();
        String s = "Select b.id,a.rym_id from temp_rating a left join artist b " +
                " on  " +
                " concat(a.first_name,' ',a.last_name)= name ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long artist_id = resultSet.getLong(1);
                long rymId = resultSet.getLong(2);
                returnedMap.put(rymId, artist_id);

            }
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
        return returnedMap;
    }


    @Override
    public Map<Long, Long> findArtistsAuxiliar(Connection connection) {
        HashMap<Long, Long> returnedMap = new HashMap<>();
        String s = "Select b.id,a.rym_id from temp_rating a left join artist b " +
                "on soundex(a.last_name) = soundex(name)  ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long artistId = resultSet.getLong(1);
                long rymId = resultSet.getLong(2);
                returnedMap.put(rymId, artistId);

            }
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
        return returnedMap;
    }

    @Override
    public void insertRatings(Connection connection, List<RYMImportRating> knownAlbums, long ownerId) {
        String queryBody =
                "                                insert into album_rating(artist_id,album_id,discord_id,rating,source,review) values %s";
        String sql = String.format(queryBody, knownAlbums.isEmpty() ? null : String.join(",", Collections.nCopies(knownAlbums.size(), "(?,?,?,?,0,?)")));
        sql += " on duplicate key UPDATE rating =  GREATEST(rating,VALUES(rating))";
        try (PreparedStatement preparedStatement2 = connection.prepareStatement(sql)) {

            int i = 1;
            for (RYMImportRating rating : knownAlbums) {
                preparedStatement2.setLong(i++, rating.getArtist_id());
                preparedStatement2.setLong(i++, rating.getId());
                preparedStatement2.setLong(i++, ownerId);
                preparedStatement2.setByte(i++, rating.getRating());
                preparedStatement2.setString(i++, rating.getReview());
            }
            preparedStatement2.execute();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void cleanUp(Connection connection) {
        String queryBody = "drop table if EXISTS  temp_rating";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public AlbumRatings getRatingsByName(Connection connection, long guildId, String album, long artistId) {

        String s = "Select a.discord_id,d.rym_id,d.album_name,c.rating,d.release_year, exists (select discord_id  from user_guild where discord_id = a.discord_id and guild_id = ? )  as owner " +
                "from user a  " +
                "join album_rating c on a.discord_id = c.discord_id " +
                "join album d on c.album_id = d.id " +
                "and d.album_name = ? " +
                "and c.artist_id = ?" +
                " order by  c.rating desc ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setLong(1, guildId);
            preparedStatement.setString(2, album);

            preparedStatement.setLong(3, artistId);

            ResultSet resultSet = preparedStatement.executeQuery();
            //  Year year = null;
            new dao.entities.AlbumRatings(artistId, 0L, "", album, null, null);
            List<Rating> ratings = new ArrayList<>();
            boolean checked = false;
            Long albumId = null;
            Year releaseYear = null;
            while (resultSet.next()) {
                if (!checked) {
                    checked = true;
                    short release_year = resultSet.getShort("release_year");
                    releaseYear = release_year == 0 ? null : Year.of(release_year);
                }

                Byte rating = resultSet.getByte("rating");
                long discord_id = resultSet.getLong("discord_id");
                boolean isThisGuild = resultSet.getBoolean("owner");
                ratings.add(new Rating(discord_id, rating, isThisGuild));

//                returnedMap.put(rymId, artistId);
            }
            return new dao.entities.AlbumRatings(0L, artistId, "", album, ratings, releaseYear);
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
    }

    @Override
    @Nullable
    public Rating getUserAlbumRating(Connection connection, long userId, long albumId, long artistId) {
        String s = "Select c.rating " +
                "from  album_rating c  " +
                "where c.artist_id = ?" +
                " and c.album_id = ? and c.discord_id = ?  " +
                " order by  c.rating desc ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setLong(1, artistId);
            preparedStatement.setLong(2, albumId);
            preparedStatement.setLong(3, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Rating(userId, resultSet.getByte(1), true);
            }
            return null;
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }

    }

    @Override
    public void deletePartialTempTable(Connection connection, Set<Long> idsToWipe) {
        String s = "Delete from temp_rating where rym_id in (%s)";
        String s1 = idsToWipe.isEmpty() ? null : String.join(",", Collections.nCopies(idsToWipe.size(), "?"));
        String query = String.format(s, s1);


        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int i = 1;
            for (Long aLong : idsToWipe) {
                preparedStatement.setLong(i++, aLong);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new ChuuServiceException(exception);
        }
    }

    @Override
    public Collection<AlbumRatings> getArtistRatings(Connection connection, long guildId, long artistId) {
        Map<String, AlbumRatings> retunMap = new HashMap<>();
        String s = "Select a.discord_id,d.rym_id,d.album_name,c.rating,d.release_year, " +
                "exists (select discord_id  from user_guild where discord_id = a.discord_id and guild_id = ? )  as owner " +
                "from user a  " +
                "join album_rating c on a.discord_id = c.discord_id " +
                "join album d on c.album_id = d.id " +
                "and c.artist_id = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setLong(1, guildId);
            preparedStatement.setLong(2, artistId);

            ResultSet resultSet = preparedStatement.executeQuery();
            //  Year year = null;
            new dao.entities.AlbumRatings(artistId, 0L, "", "", null, null);
            List<Rating> ratings = new ArrayList<>();
            while (resultSet.next()) {

                Byte rating = resultSet.getByte("rating");
                String album = resultSet.getString("album_name");

                short release_year = resultSet.getShort("release_year");
                Year releaseYear = release_year == 0 ? null : Year.of(release_year);
                long discord_id = resultSet.getLong("discord_id");
                boolean isThisGuild = resultSet.getBoolean("owner");
                AlbumRatings albumRatings = retunMap.get(album);
                if (albumRatings == null) {
                    albumRatings = new AlbumRatings(0L, artistId, "", album, new ArrayList<>(), releaseYear);
                    retunMap.put(album, albumRatings);
                }
                albumRatings.getUserRatings().add(new Rating(discord_id, rating, isThisGuild));
//                returnedMap.put(rymId, artistId);
            }
            return retunMap.values();
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }
    }

    @Override
    public List<ScoredAlbumRatings> getGlobalTopRatings(Connection connection) {
        List<ScoredAlbumRatings> returnList = new ArrayList<>();
        String s = "select *  from (select  album_name, count(*) as  coun, 0, avg(rating) as ave, name,c.url " +
                "from album_rating a " +
                "join artist b on a.artist_id = b.id " +
                "join album c on a.album_id = c.id " +
                "group by album_id) main " +
                "order by ((0.5 * main.ave) + 10 * (1 - 0.5) * (1 - (EXP(-main.coun/5))))  desc limit 200";
        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            getScoredAlbums(returnList, resultSet);
        } catch (
                SQLException throwables) {

            throw new ChuuServiceException(throwables);
        }
        return returnList;
    }

    //
    @Override
    public List<ScoredAlbumRatings> getServerTopRatings(Connection connection, long guildId) {
        List<ScoredAlbumRatings> returnList = new ArrayList<>();

        String s = "select *  from (select  album_name, count(*) as  coun, 1, avg(rating) as ave, name,c.url " +
                "from album_rating a " +
                "join artist b on a.artist_id = b.id " +
                "join album c on a.album_id = c.id " +
                "join user_guild d on a.discord_id = d.discord_id " +
                " where guild_id = ? " +
                "group by album_id) main " +
                "order by ((0.5 * main.ave) + 10 * (1 - 0.5) * (1 - (EXP(-main.coun/5))))  desc limit 200";
        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setLong(1, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            getScoredAlbums(returnList, resultSet);
        } catch (
                SQLException throwables) {

            throw new ChuuServiceException(throwables);
        }
        return returnList;
    }

    private void getScoredAlbums(List<ScoredAlbumRatings> returnList, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String albumName = resultSet.getString(1);
            long count = resultSet.getLong(2);
            double average = resultSet.getDouble(4);
            String artist = resultSet.getString(
                    5);
            String url = resultSet.getString(6);

            returnList.add(new ScoredAlbumRatings(0, albumName, url, count, average, artist));

        }
    }

    @Override
    public List<ScoredAlbumRatings> getSelfRatingsScore(Connection connection, Short ratingNumber, long discordId) {
        List<ScoredAlbumRatings> returnList = new ArrayList<>();
        String s = "select  album_name,a.id, name,rating,(select avg(rating) from album_rating t  where t.album_id = a.album_id) as agg,(select count(*) from album_rating t  where t.album_id = a.album_id) as coun,c.url " +
                "from album_rating a " +
                "join artist b on a.artist_id = b.id " +
                "join album c on a.album_id = c.id " +
                " where a.discord_id = ? ";
        if (ratingNumber != null) {
            s += " and rating = ? ";
        } else {
            s += " order by rating desc,a.id asc ";
        }
        s += " limit 200";
        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setLong(1, discordId);
            if (ratingNumber != null)
                preparedStatement.setShort(2, ratingNumber);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String albumName = resultSet.getString(1);
                String artist = resultSet.getString(3);
                short rating = resultSet.getShort(4);
                double avg = resultSet.getDouble(5);
                long count = resultSet.getLong(6);
                String url = resultSet.getString(7);


                returnList.add(new ScoredAlbumRatings(rating, albumName, url, count, avg, artist));

            }
        } catch (
                SQLException throwables) {

            throw new ChuuServiceException(throwables);
        }
        return returnList;
    }

    @Override
    public RymStats getUserRymStatms(Connection connection, long discordId) {
        List<ScoredAlbumRatings> returnList = new ArrayList<>();
        String s = "select  count(*) as  coun,  avg(rating) as ave " +
                "from album_rating a " +
                "where discord_id = ? ";
        return getRymStats(connection, discordId, s);

    }

    @Override
    public Map<Integer, Integer> getUserCurve(Connection connection, long discordId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("Select rating,count(*) from album_rating a where discord_id = ? group by  rating ")) {
            preparedStatement.setLong(1, discordId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<Integer, Integer> ratingsMap = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toMap(x -> x, x -> 0));
            while (resultSet.next()) {
                int rating = resultSet.getInt(1);
                int count = resultSet.getInt(2);
                ratingsMap.put(rating, count);
            }
            return ratingsMap;
        } catch (
                SQLException throwables) {

            throw new ChuuServiceException(throwables);
        }
    }

    @NotNull
    private RymStats getRymStats(Connection connection, long discordId, String s) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setLong(1, discordId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                double average = resultSet.getDouble(2);
                return new RymStats(count, average);
            }
            return new RymStats(0, 0);
        } catch (
                SQLException throwables) {

            throw new ChuuServiceException(throwables);
        }
    }

    @Override
    public RymStats getServerStats(Connection connection, long guildId) {
        String s = "select  count(*) as  coun,  avg(rating) as ave " +
                "from album_rating a " +
                "join user_guild c on a.discord_id = c.discord_id  " +
                "where c.guild_id = ? ";


        return getRymStats(connection, guildId, s);

    }

    @Override
    public RymStats getRYMBotStats(Connection connection) {
        String s = "select  count(*) as  coun,  avg(rating) as ave, ? as t " +
                "from album_rating a";


        return getRymStats(connection, 1L, s);
    }

    @Override
    public List<AlbumPlays> unratedAlbums(Connection connection, long discordId) {
        List<AlbumPlays> returnList = new ArrayList<>();
        String sql = "select a.album_name, d.name, b.playnumber from album a join " +
                "scrobbled_album b on a.id = b.album_id  " +
                "join user c on b.lastfm_id = c.lastfm_id  " +
                " join artist d on a.artist_id = d.id" +
                " " +
                "where c.discord_id  = ?" +
                " and a.id not in (select album_id from album_rating where c.discord_id = ? ) " +
                " order by playnumber desc ";
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, discordId);
            preparedStatement.setLong(2, discordId);


            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String albumName = resultSet.getString(1);
                String artist = resultSet.getString(2);
                int plays = resultSet.getInt(3);

                returnList.add(new AlbumPlays(artist, plays, albumName));

            }
        } catch (
                SQLException throwables) {

            throw new ChuuServiceException(throwables);
        }
        return returnList;

    }

    @Override
    public RYMAlbumStats getServerAlbumStats(Connection connection, long guildId, long artistId, long albumId) {
        String s = "Select avg(c.rating),count(c.rating)  " +
                "from user a join user_guild b on a.discord_id = b.discord_id " +
                "  " +
                "join album_rating c on b.discord_id = c.discord_id " +
                "where c.artist_id = ?" +
                "and b.guild_id = ? " +
                "and c.album_id = ? " +
                " order by  c.rating desc ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setLong(1, artistId);
            preparedStatement.setLong(2, guildId);
            preparedStatement.setLong(3, albumId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new RYMAlbumStats(resultSet.getDouble(0), resultSet.getInt(1));
            }
            return new RYMAlbumStats(0d, 0);
        } catch (SQLException throwables) {
            throw new ChuuServiceException(throwables);
        }

    }

}
