package dao.entities;

import java.time.Instant;

public record Friend(SimpleUser first, SimpleUser second, FriendStatus friendStatus, Instant when) {


    public SimpleUser other(long discordId) {
        if (first.discordId() == discordId) {
            return second;
        }
        return first;
    }

    public enum FriendStatus {
        PENDING_FIRST, PENDING_SECOND, ACCEPTED
    }

    public record UsersSorted(long first, long second) {
        public UsersSorted(long first, long second) {
            if (first > second) {
                this.first = second;
                this.second = first;
            } else {
                this.first = first;
                this.second = second;
            }
        }
    }

}
