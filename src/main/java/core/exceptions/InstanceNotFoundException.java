package core.exceptions;

public class InstanceNotFoundException extends Exception {
    private final String lastFMName;
    private final long discordId;

    public InstanceNotFoundException(String message) {
        super();
        this.lastFMName = message;
        this.discordId = -1;
    }

    public InstanceNotFoundException(long discordId) {
        super();
        this.discordId = discordId;
        this.lastFMName = null;
    }

    /**
     * @return Template that contains two "variables": ${user_to_replace} and ${prefix}
     */
    public static String getInstanceNotFoundTemplate() {
        return "**${user_to_replace}** has not set their last.fm account\n" +
               "To link to the bot you must have a last.fm account and then do:\n " +
               "${prefix}" + "set ``your_last_fm_account``";
    }

    public String getLastFMName() {
        return lastFMName;
    }


    public long getDiscordId() {
        return discordId;
    }

}
