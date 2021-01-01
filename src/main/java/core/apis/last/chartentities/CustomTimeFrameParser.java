package core.apis.last.chartentities;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.TopEntity;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.params.ChartParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.BiFunction;

public class CustomTimeFrameParser implements TimeFrameParserObtainer {
    private final ConcurrentLastFM lastFM;
    private final String lastfmId;
    private final ChartParameters chartParameters;
    private final TopEntity topEntity;
    private final CustomTimeFrame timeFrameEnum;
    private final ChuuService chuuService;
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;


    public CustomTimeFrameParser(ConcurrentLastFM lastFM, String lastfmId, ChartParameters chartParameters, TopEntity topEntity, CustomTimeFrame timeFrameEnum, ChuuService chuuService) {
        this.lastFM = lastFM;
        this.lastfmId = lastfmId;
        this.chartParameters = chartParameters;
        this.topEntity = topEntity;
        this.timeFrameEnum = timeFrameEnum;
        this.chuuService = chuuService;
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
    }

    @Override
    public BiFunction<JSONObject, Integer, UrlCapsule> obtainParse() {
        CustomTimeFrame.Type type = timeFrameEnum.getType();
        assert type != CustomTimeFrame.Type.NORMAL;
        switch (topEntity) {
            case ARTIST:
                return (artistObj, size) -> {
                    String artistName = artistObj.getString("name");
                    int plays = artistObj.getInt("playcount");
                    String mbid = artistObj.getString("mbid");
                    return new ArtistChart(null, size, artistName, mbid, plays, chartParameters.isWriteTitles(), chartParameters.isWritePlays());
                };

            case TRACK:
                return (trackObj, size) -> {
                    String name = trackObj.getString("name");
                    int frequency = trackObj.getInt("playcount");
                    String artistName = trackObj.getJSONObject("artist").getString("#text");
                    JSONArray image = trackObj.getJSONArray("image");
                    JSONObject bigImage = image.getJSONObject(image.length() - 1);
                    return new TrackChart(bigImage.getString("#text"), size, name, artistName, null, frequency, chartParameters.isWriteTitles(), chartParameters.isWritePlays());
                };
            case ALBUM:
                return (albumObj, size) ->
                {
                    JSONObject artistObj = albumObj.getJSONObject("artist");
                    String albumName = albumObj.getString("name");
                    String artistName = artistObj.getString("#text");
                    String mbid = albumObj.getString("mbid");
                    int plays = albumObj.getInt("playcount");
                    String url;
                    try {
                        url = CommandUtil.albumUrl(chuuService, lastFM, artistName, albumName, discogsApi, spotifyApi);
                    } catch (LastFmException lastFmException) {
                        url = null;
                    }
                    return new AlbumChart(url, size, albumName, artistName, mbid, plays, chartParameters.isWriteTitles(), chartParameters.isWritePlays(), chartParameters.isAside());
                };
            default:
                throw new IllegalStateException("Unexpected value: " + topEntity);
        }
    }
}
