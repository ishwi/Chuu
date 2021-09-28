package core.parsers.params;

import core.commands.Context;
import core.commands.stats.AffinityCommand;
import dao.entities.LastFMData;

public class AffinityParameters extends CommandParameters {
    private final boolean doServer;
    private final LastFMData firstUser;
    private final LastFMData secondUser;
    private final Long firstDiscordID;
    private final Long secondDiscordID;
    private final long threshold;


    public AffinityParameters(Context e, boolean doServer, LastFMData firstUser, LastFMData secondLastfmId, Long firstDiscordID, Long secondDiscordID, Long threshold) {
        super(e);
        this.doServer = doServer;
        this.firstUser = firstUser;
        this.secondUser = secondLastfmId;

        this.firstDiscordID = firstDiscordID;
        this.secondDiscordID = secondDiscordID;
        this.threshold = threshold == null ? AffinityCommand.DEFAULT_THRESHOLD : threshold;
    }

    public boolean isDoServer() {
        return doServer;
    }

    public LastFMData getFirstUser() {
        return firstUser;
    }

    public LastFMData getSecondUser() {
        return secondUser;
    }

    public Long getFirstDiscordID() {
        return firstDiscordID;
    }

    public Long getSecondDiscordID() {
        return secondDiscordID;
    }

    public long getThreshold() {
        return threshold;
    }
}
