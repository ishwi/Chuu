package dao.entities;

import core.commands.AffinityCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Affinity {
    private final static long closeMatchWeight = 10;
    private final static long trueMatchWeight = 25;
    private final long threshold;
    private final long matchingCount;
    private final long closeMatch;
    private final long trueMatching;
    private final long sampleSize;
    private final List<UserArtistComparison> matchingList;
    private final String ogLastFmId;
    private final String receivingLastFmId;
    private long discordId;
    private float percentage;
    private boolean hasCalculated;

    private String ogRec;
    private String receivingRec;

    public Affinity(long threshold, long matchingCount, long closeMatch, long trueMatching, long sampleSize, String ogLastFmId, String receivingLastFmId) {
        this.threshold = threshold;
        this.matchingCount = matchingCount;
        this.closeMatch = closeMatch;
        this.trueMatching = trueMatching;
        this.sampleSize = sampleSize;
        this.ogLastFmId = ogLastFmId;
        this.receivingLastFmId = receivingLastFmId;
        this.matchingList = new ArrayList<>();
    }

    public float getAffinity() {
        if (!this.hasCaculated()) {
            this.percentage = (((this.matchingCount + (this.closeMatch * closeMatchWeight) + (this.trueMatching * trueMatchWeight)) * ((float) threshold / AffinityCommand.DEFAULT_THRESHOLD))) / (sampleSize + 1);
        }
        return this.percentage;

    }

    synchronized private boolean hasCaculated() {
        return hasCalculated;
    }

    public void addMatchings(Collection<UserArtistComparison> matchings) {
        this.matchingList.addAll(matchings);
    }

    public long getMatchingCount() {
        return matchingCount;
    }

    public long getCloseMatch() {
        return closeMatch;
    }

    public long getTrueMatching() {
        return trueMatching;
    }

    public List<UserArtistComparison> getMatchingList() {
        return matchingList;
    }

    public String getOgRec() {
        return ogRec;
    }

    public void setOgRec(String ogRec) {
        this.ogRec = ogRec;
    }

    public String getReceivingRec() {
        return receivingRec;
    }

    public void setReceivingRec(String receivingRec) {
        this.receivingRec = receivingRec;
    }

    public String getOgLastFmId() {
        return ogLastFmId;
    }

    public String getReceivingLastFmId() {
        return receivingLastFmId;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public long getThreshold() {
        return threshold;
    }
}
