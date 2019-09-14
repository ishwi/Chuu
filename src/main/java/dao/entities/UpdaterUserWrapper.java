package dao.entities;

public class UpdaterUserWrapper extends UsersWrapper {
	private int timestampControl;

	public UpdaterUserWrapper(long discordID, String lastFMName, int timestamp, int timestampControl) {
		super(discordID, lastFMName, timestamp);
		this.timestampControl = timestampControl;
	}

	public int getTimestampControl() {
		return timestampControl;
	}

	public void setTimestampControl(int timestampControl) {
		this.timestampControl = timestampControl;
	}
}
