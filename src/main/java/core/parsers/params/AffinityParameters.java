package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AffinityParameters extends CommandParameters {
    private final boolean doServer;
    private final String firstLastfmId;
    private final String secondLastfmId;
    private final Long firstDiscordID;
    private final Long secondDiscordID;
    private final Integer threshold;


    public AffinityParameters(MessageReceivedEvent e, boolean doServer, String firstLastfmId, String secondLastfmId, Long firstDiscordID, Long secondDiscordID, Integer threshold) {
        super(e);
        this.doServer = doServer;
        this.firstLastfmId = firstLastfmId;
        this.secondLastfmId = secondLastfmId;

        this.firstDiscordID = firstDiscordID;
        this.secondDiscordID = secondDiscordID;
        this.threshold = threshold;
    }

    public boolean isDoServer() {
        return doServer;
    }

    public String getFirstLastfmId() {
        return firstLastfmId;
    }

    public String getSecondLastfmId() {
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
