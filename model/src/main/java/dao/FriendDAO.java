package dao;

import dao.entities.Friend;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface FriendDAO {

    List<Friend> getUserFriends(Connection connection, long discordId);

    List<Long> getUserFriendsIds(Connection connection, long discordId);

    List<Friend> getIncomingRequests(Connection connection, long discordId);

    List<Friend> getPendingRequests(Connection connection, long discordId);

    boolean acceptRequest(Connection connection, long discordId, long requesterId);

    void createRequest(Connection connection, long discordId, long receiverId);


    void rejectRequest(Connection connection, long discordId, long requesterId);

    Optional<Friend> areFriends(Connection connection, long discordId, long receiverId);

}
