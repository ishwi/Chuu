package core.apis.last;


import core.apis.last.chartentities.ArtistChart;
import core.parsers.params.ChartParameters;
import dao.entities.ArtistInfo;
import dao.entities.NowPlayingArtist;
import dao.entities.UrlCapsule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum TopEntity {
    ALBUM, TRACK, ARTIST;


    String getLeadingObject() {
        String returnValue;
        switch (this) {
            case ALBUM:
                returnValue = "topalbums";
                break;
            case TRACK:
                returnValue = "toptracks";
                break;
            case ARTIST:
                returnValue = "topartists";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        return returnValue;
    }

    String getApiMethod() {
        String returnValue;
        switch (this) {
            case ALBUM:
                returnValue = ConcurrentLastFM.GET_ALBUMS;
                break;

            case TRACK:
                returnValue = ConcurrentLastFM.GET_TOP_TRACKS;
                break;
            case ARTIST:
                returnValue = ConcurrentLastFM.GET_ARTIST;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        return returnValue;
    }

    String getArrayObject() {
        String returnValue;
        switch (this) {
            case ALBUM:
                returnValue = "album";
                break;
            case TRACK:
                returnValue = "track";
                break;
            case ARTIST:
                returnValue = "artist";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        return returnValue;
    }


    public List<UrlCapsule> getDailyParser(List<NowPlayingArtist> nowPlayingArtists, ChartParameters chartParameters) {
        switch (this) {

            case ARTIST:
                Map<ArtistInfo, ArtistChart> b = nowPlayingArtists.stream().collect(
                        Collectors.toMap(x -> new ArtistInfo(x.getUrl(), x.getArtistName()),
                                x -> new ArtistChart(x.getUrl(), 0, x.getArtistName(), x.getMbid(), 1,
                                        chartParameters.isWriteTitles()
                                        , chartParameters.isWritePlays()), (y, z) -> {
                                    y.setPlays(y.getPlays() + z.getPlays());
                                    return y;
                                }));
                return new ArrayList<>(b.values());
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }


}