package core.exceptions;

import dao.entities.TimeFrameEnum;

import java.util.Arrays;
import java.util.Optional;

public class LastFMNoPlaysException extends LastFmException {
    private final String username;
    private final String timeFrameEnum;

    public LastFMNoPlaysException(String message) {

        super("");
        username = message;
        timeFrameEnum = TimeFrameEnum.ALL.toString();

    }

    public LastFMNoPlaysException(String username, String apiTimeFrame) {
        super("");

        this.username = username;
        if (apiTimeFrame.equals("day")) {
            timeFrameEnum = "day";
        } else {
            Optional<TimeFrameEnum> first = Arrays.stream(TimeFrameEnum.values())
                    .filter(x -> x.toApiFormat().equals(apiTimeFrame)).findFirst();
            assert first.isPresent();
            this.timeFrameEnum = first.get().toString();
        }
    }

    public String getUsername() {
        return username;
    }

    public String getTimeFrameEnum() {
        return timeFrameEnum;
    }

}


