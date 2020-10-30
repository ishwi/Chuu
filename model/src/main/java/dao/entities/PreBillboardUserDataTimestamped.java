package dao.entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class PreBillboardUserDataTimestamped extends PreBillboardUserData implements Serializable {
    private static final long serialVersionUID = 1231231L;

    private final Timestamp timestamp;

    public PreBillboardUserDataTimestamped(long artistId, String lastfmId, String trackName, int playCount, Timestamp timestamp) {
        super(artistId, lastfmId, trackName, playCount);
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
