package core.parsers.params;

import core.commands.Context;
import core.commands.stats.AffinityCommand;
import dao.entities.LastFMData;

public class AffinityParameters extends CommandParameters {
    private final boolean doServer;
    private final LastFMData firstLastfmId;
    private final LastFMData secondLastfmId;
    private final Long firstDiscordID;
    private final Long secondDiscordID;
    private final Integer threshold;


    public AffinityParameters(Context e, boolean doServer, LastFMData firstLastfmId, LastFMData secondLastfmId, Long firstDiscordID, Long secondDiscordID, Integer threshold) {
        super(e);
        this.doServer = doServer;
        this.firstLastfmId = firstLastfmId;
        this.secondLastfmId = secondLastfmId;

        this.firstDiscordID = firstDiscordID;
        this.secondDiscordID = secondDiscordID;
        this.threshold = threshold == null ? AffinityCommand.DEFAULT_THRESHOLD : threshold;
    }

    public boolean isDoServer() {
        return doServer;
    }

    public LastFMData getFirstLastfmId() {
        return firstLastfmId;
    }

    public LastFMData getSecondLastfmId() {
        return secondLastfmId;
    }

    public Long getFirstDiscordID() {
        return firstDiscordID;
    }

    public Long getSecondDiscordID() {
        return secondDiscordID;
    }

    public Integer getThreshold() {
        return threshold;
    }
}
