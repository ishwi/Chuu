package core.exceptions;

public class UnknownLastFmException extends LastFmException {
    private final int code;
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

    public UnknownLastFmException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getSentMessage() {
        if (code == 17) {
            return "Maybe you still need to activate your account on last.fm?";
        } else return getMessage();
    }
}
