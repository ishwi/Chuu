package core.util.stats;

import core.apis.last.ConcurrentLastFM;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledTrack;
import dao.entities.UserInfo;

public record StatsCtx(LastFMData lastFMData, ChuuService chuuService, ConcurrentLastFM lastFM, UserInfo userInfo,
                       int totalPlays,
                       int timestamp,
                       CustomTimeFrame timeFrameEnum, Integer count, ScrobbledTrack np) {

}
