package core.services;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.commands.utils.CommandUtil;
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

    public UserInfo getUserInfo(LastFMData user) {
        return service.getUserInfo(user.getName()).orElseGet(() ->
        {
            UserInfo userInfo;
            try {
                userInfo = lastFM.getUserInfo(List.of(user.getName()), user).get(0);
                service.insertUserInfo(userInfo);
                return userInfo;
            } catch (LastFmException lastFmException) {
                return new UserInfo(0, "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png", user.getName(), Math.toIntExact(Instant.now().getEpochSecond()));
            }
        });
    }

    public UserInfo maybeRefresh(LastFMData user) throws LastFmException {
        if (CommandUtil.rand.nextFloat() > 0.85f) {
            return refreshUserInfo(user);
        } else {
            return getUserInfo(user);
        }
    }

    public UserInfo refreshUserInfo(LastFMData user) throws LastFmException {
        UserInfo userInfo = lastFM.getUserInfo(List.of(user.getName()), user).get(0);
        service.insertUserInfo(userInfo);
        return userInfo;
    }
}
