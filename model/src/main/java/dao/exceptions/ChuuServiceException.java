package dao.exceptions;

public class ChuuServiceException extends RuntimeException {
    public ChuuServiceException() {
    }

    public ChuuServiceException(String message) {
        super(message);
    }

    public ChuuServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChuuServiceException(Throwable cause) {
        super(cause);
    }
}
