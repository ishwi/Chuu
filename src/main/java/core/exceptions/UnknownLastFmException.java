package core.exceptions;

import dao.entities.LastFMData;
import org.jetbrains.annotations.Nullable;

public class UnknownLastFmException extends LastFmException {
    public static final int INVALID_SERVICE = 2;
    public static final int INVALID_METHOD = 3;
    public static final int AUTHENTICATOIN_FAILED = 4;
    public static final int INVALID_FORMAT = 5;
    public static final int INVALID_PARAMETERS = 6;
    public static final int INVALID_RESOURCE = 7;
    public static final int OPERATION_FAILED = 8;
    public static final int INVALID_SESSION_KEY = 9;
    public static final int INVALID_API_KEY = 10;
    public static final int SERVICE_OFFLINE = 11;
    public static final int INVALID_METHOD_SIGNATURE = 13;
    public static final int TEMPORARY_ERROR = 16;
    public static final int SUSPENDED_API_KEY = 26;
    public static final int RATE_LIMIT_EXCEEDED = 29;
    private final int code;
    @Nullable
    private final LastFMData user;

    public UnknownLastFmException(String message, int code, @Nullable LastFMData user) {
        super(message);
        this.code = code;
        this.user = user;
    }

    public int getCode() {
        return code;
    }

    public String getSentMessage() {
        return switch (code) {
            case 17 -> "Maybe you still need to activate your account on last.fm?";
            case INVALID_SESSION_KEY -> "Your session key has been invalidated. Login again pls :(";
            case RATE_LIMIT_EXCEEDED -> "The bot has been globally ratelimited by last.fm, give the bot a few mins to rest and try again later :(";
            default -> getMessage();
        };
    }

    @Nullable
    public LastFMData getUser() {
        return user;
    }
}
