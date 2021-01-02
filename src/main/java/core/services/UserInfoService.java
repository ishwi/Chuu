package core.services;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.UserInfo;

import java.time.Instant;
import java.util.List;

public class UserInfoService {
    private final ChuuService service;
    private final ConcurrentLastFM lastFM;

    public UserInfoService(ChuuService service) {
        this.service = service;
        this.lastFM = LastFMFactory.getNewInstance();
    }

    public UserInfo getUserInfo(LastFMData lastfmId) {
        return service.getUserInfo(lastfmId.getName()).orElseGet(() ->
        {
            UserInfo userInfo;
            try {
                userInfo = lastFM.getUserInfo(List.of(lastfmId.getName()), lastfmId).get(0);
                service.insertUserInfo(userInfo);
                return userInfo;
            } catch (LastFmException lastFmException) {
                return new UserInfo(0, "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png", lastfmId.getName(), Math.toIntExact(Instant.now().getEpochSecond()));
            }
        });
    }

    public UserInfo refreshUserInfo(LastFMData lastfmId) {
        try {
            UserInfo userInfo = lastFM.getUserInfo(List.of(lastfmId.getName()), lastfmId).get(0);
            service.insertUserInfo(userInfo);
            return userInfo;
        } catch (LastFmException lastFmException) {
            lastFmException.printStackTrace();
        }
        return getUserInfo(lastfmId);
    }
}
