package core.services;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.exceptions.UnknownLastFmException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.ChuuServiceException;
import org.jetbrains.annotations.Nullable;

public record OAuthService(ChuuService db, ConcurrentLastFM lastFM) {

    private static void handleInterrupt() {
        if (Thread.interrupted()) {
            throw new ChuuServiceException(new InterruptedException());
        }
    }

    public String generateURL(String url, @Nullable LastFMData lastFMData) throws LastFmException {
        if (lastFMData == null) {
            return url;
        }
        try {
            if (lastFMData.getToken() != null) {
                String authSession = lastFM.getAuthSession(lastFMData);
                db.storeSess(authSession, lastFMData.getName());
                lastFMData.setSession(authSession);
                handleInterrupt();
            }
            if (lastFMData.getSession() != null) {
                return lastFM.generateAuthoredCall(url, lastFMData.getSession());
            }
            return url;
        } catch (UnknownLastFmException exception) {
            if (exception.getCode() == 9) {
                db.clearSess(lastFMData.getName());
                handleInterrupt();
            }
            throw exception;
        }


    }
}
