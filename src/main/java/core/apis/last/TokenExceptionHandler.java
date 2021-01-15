package core.apis.last;

import core.exceptions.UnknownLastFmException;
import dao.ChuuService;

public record TokenExceptionHandler(UnknownLastFmException exception, ChuuService dao) {

    public String handle() {
        int code = exception.getCode();
        if (exception.getUser() != null) {
            return switch (code) {
                case 4 -> {
                    dao.storeToken(null, exception.getUser().getName());
                    yield "You didn't authorize the bot properly. Try to do `login` again if you want to authorize it";
                }
                case 14, 9 -> {
                    dao.storeSess(null, exception.getUser().getName());
                    yield "The bot has been deauthorized. Try to do `login` again if you want to authorize it again";
                }

                default -> "Unknown last.fm exception found:\n" + exception.getSentMessage();
            };
        }
        return "Unknown last.fm exception found:\n" + exception.getSentMessage();
    }
}
