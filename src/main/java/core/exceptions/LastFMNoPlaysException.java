package core.exceptions;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.TimeFrameEnum;

public class LastFMNoPlaysException extends LastFmException {
    private final String username;
    private final CustomTimeFrame timeFrameEnum;

    public LastFMNoPlaysException(String message) {

        super("");
        username = message;
        timeFrameEnum = CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL);

    }

    public LastFMNoPlaysException(String username, CustomTimeFrame customTimeFrame) {
        super("");

        this.username = username;
        timeFrameEnum = customTimeFrame;
    }

    public String getUsername() {
        return username;
    }

    public String getTimeFrameEnum() {
        return timeFrameEnum.getDisplayString();
    }

}


