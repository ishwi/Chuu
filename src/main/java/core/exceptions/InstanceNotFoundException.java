package core.exceptions;

public class InstanceNotFoundException extends Exception {
	private String lastFMName;
	private long discordId;

	public InstanceNotFoundException(String message) {
		super();
		this.lastFMName = message;
	}

	public InstanceNotFoundException(long discordId) {
		super();
		this.discordId = discordId;
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

	public void setLastFMName(String lastFMName) {
		this.lastFMName = lastFMName;
	}

	public long getDiscordId() {
		return discordId;
	}

	public void setDiscordId(long discordId) {
		this.discordId = discordId;
	}
}
