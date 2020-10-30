package dao.entities;

import java.util.TimeZone;

public class UpdaterUserWrapper extends UsersWrapper {
    private int timestampControl;

    public UpdaterUserWrapper(long discordID, String lastFMName, int timestamp, int timestampControl, Role role, TimeZone timeZone) {
        super(discordID, lastFMName, timestamp, role, timeZone);
        this.timestampControl = timestampControl;
    }

    public int getTimestampControl() {
        return timestampControl;
    }

    public void setTimestampControl(int timestampControl) {
        this.timestampControl = timestampControl;
    }
}
