package dao;

import dao.entities.RYMImportRating;
import dao.entities.ScoredAlbumRatings;
import core.exceptions.ChuuServiceException;
import dao.entities.AlbumRatings;
import dao.entities.Rating;
import dao.entities.RymStats;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.*;

public class SQLRYMDaoImpl implements SQLRYMDao {
    @Override
    public void setServerTempTable(Connection connection, List<RYMImportRating> ratings) {

        @Language("MariaDB") String queryBody =
                "                                CREATE TEMPORARY TABLE temp_rating(\n" +
                        "                                        rym_id bigint(20) PRIMARY KEY,\n" +
                        "                                        last_name varchar(400),\n" +
                        "                                        first_name varchar(20),\n" +
                        "                                        first_localized_name varchar(20),\n" +
                        "                                        last_localized_name varchar(40)\n" +
                        "                        );";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();

            queryBody =
                    "                                insert into temp_rating(rym_id,last_name,first_name,first_localized_name,last_localized_name) values %s";
            String sql = String.format(queryBody, ratings.isEmpty() ? null : String.join(",", Collections.nCopies(ratings.size(), "(?,?,?,?,?)")));
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
                "or concat(a.first_name,' ',a.last_name) = name " +
                "or  concat(a.first_localized_name,' ',a.last_localized_name) = name " +
                "or last_localized_name = name ";
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
        @Language("MariaDB") String queryBody = "drop table if EXISTS  temp_rating";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public AlbumRatings getRatingsByName(Connection connection, long idLong, String album, long artistId) {

        String s = "Select a.discord_id,d.rym_id,d.album_name,c.rating,d.release_year, exists (select discord_id  from user_guild where discord_id = a.discord_id and guild_id = ? )  as owner " +
                "from user a  " +
                "join album_rating c on a.discord_id = c.discord_id " +
                "join album d on c.album_id = d.id " +
                "and d.album_name = ? " +
                "and c.artist_id = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setLong(1, idLong);
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
                Long discord_id = resultSet.getLong("discord_id");
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
        String s = "Select a.discord_id,d.rym_id,d.album_name,c.rating,d.release_year, exists (select discord_id  from user_guild where discord_id = a.discord_id and guild_id = ? )  as owner " +
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
                Long discord_id = resultSet.getLong("discord_id");
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
        String s = "select *  from (select  album_name, count(*) as  coun, sum(rating) as agg, avg(rating) as ave, name " +
                "from album_rating a " +
                "join artist b on a.artist_id = b.id " +
                "join album c on a.album_id = c.id " +
                "group by album_id) main " +
                "order by (main.coun * main.agg * main.ave)  desc limit 200";
        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String albumName = resultSet.getString(1);
                long count = resultSet.getLong(2);
                double average = resultSet.getDouble(4);
                String artist = resultSet.getString(
                        5);
                returnList.add(new ScoredAlbumRatings(0, albumName, count, average, artist));

            }
        } catch (
                SQLException throwables) {

            throw new ChuuServiceException(throwables);
        }
        return returnList;
    }

    @Override
    public List<ScoredAlbumRatings> getSelfRatingsScore(Connection connection, Short ratingNumber, long discordId) {
        List<ScoredAlbumRatings> returnList = new ArrayList<>();
        String s = "select  album_name, name,rating,(select avg(rating) from album_rating t  where t.album_id = a.album_id) as agg,(select count(*) from album_rating t  where t.album_id = a.album_id) as coun " +
                "from album_rating a " +
                "join artist b on a.artist_id = b.id " +
                "join album c on a.album_id = c.id " +
                " where a.discord_id = ? ";
        if (ratingNumber != null) {
            s += " and rating = ? ";
        } else {
            s += " order by rating desc ";
        }
        s += " limit 200";
        try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
            preparedStatement.setLong(1, discordId);
            if (ratingNumber != null)
                preparedStatement.setShort(2, ratingNumber);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String albumName = resultSet.getString(1);
                String artist = resultSet.getString(2);
                short rating = resultSet.getShort(3);
                double avg = resultSet.getDouble(4);
                long count = resultSet.getLong(5);

                returnList.add(new ScoredAlbumRatings(rating, albumName, count, avg, artist));

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

}
