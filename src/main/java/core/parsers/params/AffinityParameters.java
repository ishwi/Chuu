package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AffinityParameters extends CommandParameters {
    private final boolean doServer;
    private final String firstLastfmId;
    private final String secondLastfmId;
    private final Long firstDiscordID;
    private final Long secondDiscordID;
    private final int threshold;


    public AffinityParameters(String[] parse, MessageReceivedEvent e, int defaultThreshold) {
        super(parse, e);
        this.doServer = Boolean.parseBoolean(parse[4]);
        if (doServer) {
            secondDiscordID = null;
            firstDiscordID = null;
            firstLastfmId = null;
            secondLastfmId = null;
        } else {
            firstDiscordID = Long.valueOf(parse[0]);
            secondDiscordID = Long.valueOf(parse[2]);
            firstLastfmId = parse[1];
            secondLastfmId = parse[3];
        }
        this.threshold = parse[5] == null ? defaultThreshold : Integer.parseInt(parse[2]);
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

    public int getThreshold() {
        return threshold;
    }
}
