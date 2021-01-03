package core.apis.last.entities.chartentities;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import org.json.JSONObject;

import java.util.function.BiFunction;

public class TimeFrameParser implements TimeFrameParserObtainer {
    private final ConcurrentLastFM lastFM;
    private final LastFMData lastfmId;
    private final ChartParameters chartParameters;
    private final TopEntity topEntity;
    private final TimeFrameEnum timeFrameEnum;


    public TimeFrameParser(ConcurrentLastFM lastFM, LastFMData lastfmId, ChartParameters chartParameters, TopEntity topEntity, TimeFrameEnum timeFrameEnum) {
        this.lastFM = lastFM;
        this.lastfmId = lastfmId;
        this.chartParameters = chartParameters;
        this.topEntity = topEntity;
        this.timeFrameEnum = timeFrameEnum;
    }

    @Override
    public BiFunction<JSONObject, Integer, UrlCapsule> obtainParse() throws LastFmException {
        switch (topEntity) {
            case ALBUM:
                if (chartParameters instanceof ChartGroupParameters) {
                    if (timeFrameEnum.equals(TimeFrameEnum.DAY)) {
                        return TrackDurationAlbumArtistChart.getDailyArtistAlbumDurationParser((ChartGroupParameters) chartParameters, lastFM.getTrackDurations(lastfmId, TimeFrameEnum.WEEK));
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
                        return TrackDurationChart.getDailyTrackParser((ChartGroupParameters) chartParameters, lastFM.getTrackDurations(lastfmId, TimeFrameEnum.WEEK));
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
                        return TrackDurationArtistChart.getDailyTrackDurationArtistParser((ChartGroupParameters) chartParameters, lastFM.getTrackDurations(lastfmId, TimeFrameEnum.WEEK));
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
