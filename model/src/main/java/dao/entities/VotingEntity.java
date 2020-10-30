package dao.entities;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class VotingEntity {
    private final String artist;
    private final String url;
    private final LocalDateTime dateTime;
    private final long owner;
    private final long artistId;
    private final AtomicInteger votes;
    private final AtomicInteger totalVotes;
    private final long urlId;
    private String userDisplay;

    public VotingEntity(String artist, String url, LocalDateTime dateTime, long owner, long artistID, int votes, int totalVotes, long urlId1) {
        this.artist = artist;
        this.url = url;
        this.dateTime = dateTime;
        this.owner = owner;
        this.artistId = artistID;
        this.votes = new AtomicInteger(votes);
        this.totalVotes = new AtomicInteger(totalVotes);
        this.urlId = urlId1;
    }

    public String getArtist() {
        return artist;
    }

    public String getUserDisplay() {
        return userDisplay;
    }

    public void setUserDisplay(String userDisplay) {
        this.userDisplay = userDisplay;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public long getOwner() {
        return owner;
    }

    public long getUrlId() {
        return urlId;
    }

    public int getVotes() {
        return votes.get();
    }

    public int add() {
        return votes.incrementAndGet();
    }

    public int decrement() {
        return votes.decrementAndGet();
    }


    public int changeToAPositive() {
        return votes.addAndGet(2);
    }

    public int changeToANegative() {
        return votes.addAndGet(-2);
    }

    public int getTotalVotes() {
        return totalVotes.get();
    }

    public int incrementTotalVotes() {
        return totalVotes.incrementAndGet();
    }

    public long getArtistId() {
        return artistId;
    }
}
