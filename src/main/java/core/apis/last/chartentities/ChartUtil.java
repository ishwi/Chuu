package core.apis.last.chartentities;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.TopEntity;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import org.json.JSONObject;

import java.util.function.BiFunction;

public class ChartUtil {


    public static BiFunction<JSONObject, Integer, UrlCapsule> getParser(TimeFrameEnum timeFrameEnum, TopEntity topEntity, ChartParameters chartParameters, ConcurrentLastFM lastFM, String username) throws LastFmException {
        switch (topEntity) {
            case ALBUM:
                if (chartParameters instanceof ChartGroupParameters) {
                    if (timeFrameEnum.equals(TimeFrameEnum.DAY)) {
                        return TrackDurationAlbumArtistChart.getDailyArtistAlbumDurationParser((ChartGroupParameters) chartParameters, lastFM.getTrackDurations(username, TimeFrameEnum.WEEK));
                    }
                    return TrackDurationAlbumArtistChart.getParser((ChartGroupParameters) chartParameters);
                } else {
                    if (timeFrameEnum.equals(TimeFrameEnum.DAY)) {
                        return AlbumChart.getDailyAlbumParser(chartParameters);
                    }
                    return AlbumChart.getAlbumParser(chartParameters);
                }

            case TRACK:
                if (chartParameters instanceof ChartGroupParameters) {
                    if (timeFrameEnum.equals(TimeFrameEnum.DAY)) {
                        return TrackDurationChart.getDailyTrackParser((ChartGroupParameters) chartParameters, lastFM.getTrackDurations(username, TimeFrameEnum.WEEK));
                    }
                    return TrackDurationChart.getTrackDurationParser((ChartGroupParameters) chartParameters);
                } else {
                    if (timeFrameEnum.equals(TimeFrameEnum.DAY)) {
                        return TrackChart.getDailyTrackParser(chartParameters);
                    }
                    return TrackChart.getTrackParser(chartParameters);
                }
            case ARTIST:
                if (chartParameters instanceof ChartGroupParameters) {
                    if (timeFrameEnum.equals(TimeFrameEnum.DAY)) {
                        return TrackDurationArtistChart.getDailyTrackDurationArtistParser((ChartGroupParameters) chartParameters, lastFM.getTrackDurations(username, TimeFrameEnum.WEEK));
                    }
                    return TrackDurationArtistChart.getTrackDurationArtistParser((ChartGroupParameters) chartParameters);
                } else {
                    if (timeFrameEnum.equals(TimeFrameEnum.DAY)) {
                        return ArtistChart.getDailyArtistParser(chartParameters);
                    }
                    return ArtistChart.getArtistParser(chartParameters);
                }
            default:
                throw new IllegalStateException("Unexpected value: " + topEntity);
        }
    }
}
