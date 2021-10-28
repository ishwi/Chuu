package dao;

import dao.entities.Friend;
import dao.entities.SimpleUser;
import dao.exceptions.ChuuServiceException;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendDAOImpl implements FriendDAO {
    @Override
    public List<Friend> getUserFriends(Connection connection, long discordId) {

        List<Friend> friends = new ArrayList<>();
        String sql = """
                Select first_user,b.lastfm_id as first_lfm, second_user,c.lastfm_id as second_lfm, created_date
                from friends a
                         join user b on a.first_user = b.discord_id
                         join user c on a.second_user = c.discord_id
                where (first_user = ?
                   or second_user = ?) and status = 'ACCEPTED'""";
        return loadFriends(connection, discordId, friends, sql);
    }

    @Override
    public List<Long> getUserFriendsIds(Connection connection, long discordId) {
        List<Long> friends = new ArrayList<>();
        String sql = """
                                
                (Select second_user
                from friends a
                         join user b on a.first_user = b.discord_id
                         join user c on a.second_user = c.discord_id
                where first_user = ?  and status = 'ACCEPTED')
                UNION ALL
                (Select first_user
                from friends a
                         join user b on a.first_user = b.discord_id
                         join user c on a.second_user = c.discord_id
                where second_user = ?  and status = 'ACCEPTED')
                """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, discordId);
            preparedStatement.setLong(2, discordId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                friends.add(resultSet.getLong(1));
            }
            return friends;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<Friend> getIncomingRequests(Connection connection, long discordId) {
        List<Friend> friends = new ArrayList<>();
        String sql = """
                Select first_user,b.lastfm_id as first_lfm, second_user,c.lastfm_id as second_lfm, created_date
                from friends a
                         join user b on a.first_user = b.discord_id
                         join user c on a.second_user = c.discord_id
                where (second_user = ? and status = 'PENDING_SECOND') or
                (first_user =? and status = 'PENDING_FIRST')""";
        return loadFriends(connection, discordId, friends, sql);
    }

    @Override
    public List<Friend> getPendingRequests(Connection connection, long discordId) {
        List<Friend> friends = new ArrayList<>();
        String sql = """
                Select first_user,b.lastfm_id as first_lfm, second_user,c.lastfm_id as second_lfm, created_date
                from friends a
                         join user b on a.first_user = b.discord_id
                         join user c on a.second_user = c.discord_id
                where (first_user = ? and status = 'PENDING_SECOND') or
                (second_user =? and status = 'PENDING_FIRST')""";
        return loadFriends(connection, discordId, friends, sql);
    }

    private List<Friend> loadFriends(Connection connection, long discordId, List<Friend> friends, String sql) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, discordId);
            preparedStatement.setLong(2, discordId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long firstUser = resultSet.getLong(1);
                String lfm = resultSet.getString(2);
                long secondUser = resultSet.getLong(3);
                String lfm2 = resultSet.getString(4);
                Instant created = resultSet.getObject(5, Timestamp.class).toInstant();
                friends.add(new Friend(new SimpleUser(lfm, firstUser), new SimpleUser(lfm2, secondUser), Friend.FriendStatus.ACCEPTED, created));
            }
            return friends;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public boolean acceptRequest(Connection connection, long discordId, long requesterId) {
        String sql = """
                update friends set status = 'ACCEPTED' where first_user = ? and second_user = ?
                """;
        Friend.UsersSorted usersSorted = new Friend.UsersSorted(discordId, requesterId);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, usersSorted.first());
            preparedStatement.setLong(2, usersSorted.second());
            int i = preparedStatement.executeUpdate();
            return i > 0;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void createRequest(Connection connection, long discordId, long receiverId) {
        String sql = """
                insert into friends(first_user,second_user,status) values (?,?,?)
                """;
        Friend.UsersSorted usersSorted = new Friend.UsersSorted(discordId, receiverId);

        Friend.FriendStatus status = (discordId == usersSorted.first()) ? Friend.FriendStatus.PENDING_SECOND : Friend.FriendStatus.PENDING_FIRST;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, usersSorted.first());
            preparedStatement.setLong(2, usersSorted.second());
            preparedStatement.setString(3, status.name());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void rejectRequest(Connection connection, long discordId, long requesterId) {
        String sql = """
                delete from friends where first_user = ? and second_user = ?
                """;
        Friend.UsersSorted usersSorted = new Friend.UsersSorted(discordId, requesterId);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, usersSorted.first());
            preparedStatement.setLong(2, usersSorted.second());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Optional<Friend> areFriends(Connection connection, long discordId, long receiverId) {
        String sql = """
                Select first_user,b.lastfm_id as first_lfm, second_user,c.lastfm_id as second_lfm, created_date,status
                 from friends a join user b on a.first_user = b.discord_id join user c on a.second_user = c.discord_id where  first_user = ? and second_user = ? limit 1
                """;
        Friend.UsersSorted usersSorted = new Friend.UsersSorted(discordId, receiverId);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, usersSorted.first());
            preparedStatement.setLong(2, usersSorted.second());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                long firstUser = resultSet.getLong(1);
                String lfm = resultSet.getString(2);
                long secondUser = resultSet.getLong(3);
                String lfm2 = resultSet.getString(4);
                Instant created = resultSet.getObject(5, Timestamp.class).toInstant();
                Friend.FriendStatus status = Friend.FriendStatus.valueOf(resultSet.getString(6));
                return Optional.of(new Friend(new SimpleUser(lfm, firstUser), new SimpleUser(lfm2, secondUser), status, created));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }
}
