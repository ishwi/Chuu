package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RecommendationsParams extends CommandParameters {
    private final boolean noUser;
    private final String firstLastfmId;
    private final String secondLastfmId;
    private final Long firstDiscordID;
    private final Long secondDiscordID;
    private final boolean showRepeated;


    public RecommendationsParams(String[] parse, MessageReceivedEvent e) {
        super(parse, e);
        this.noUser = Boolean.parseBoolean(parse[0]);
        if (noUser) {
            secondDiscordID = null;
            firstDiscordID = null;
            firstLastfmId = null;
            secondLastfmId = null;
        } else {
            firstDiscordID = Long.valueOf(parse[1]);
            secondDiscordID = Long.valueOf(parse[3]);
            firstLastfmId = parse[2];
            secondLastfmId = parse[4];
        }
        this.showRepeated = Boolean.parseBoolean(parse[5]);
    }

    public boolean isNoUser() {
        return noUser;
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

    public boolean isShowRepeated() {
        return showRepeated;
    }
}
