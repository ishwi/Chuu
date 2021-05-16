package core.services.tags;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.services.TrackValidator;
import dao.ChuuService;
import dao.entities.Genre;
import dao.entities.ScrobbledTrack;
import dao.entities.TrackInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TrackTagService extends TagService<TrackInfo, ScrobbledTrack> {
    public TrackTagService(ChuuService dao, ConcurrentLastFM lastFM, Map<Genre, List<TrackInfo>> genres) {
        super(dao, lastFM, genres);
    }

    public TrackTagService(ChuuService dao, ConcurrentLastFM lastFM, List<TrackInfo> tracks, String genre) {
        super(dao, lastFM, tracks, genre);
    }

    public TrackTagService(ChuuService dao, ConcurrentLastFM lastFM, List<String> tags, TrackInfo albumInfo) {
        super(dao, lastFM, tags, albumInfo);
    }

    @Override
    protected void insertGenres(Map<Genre, List<ScrobbledTrack>> genres) {
        dao.insertTrackTags(genres);
    }

    @Override
    protected Map<TrackInfo, ScrobbledTrack> validate(List<TrackInfo> toValidate) {
        TrackValidator trackValidator = new TrackValidator(dao, lastFM);
        return toValidate.stream().map(t -> {
            try {
                return new Holder(t, trackValidator.validate(t.getArtist(), t.getTrack()));
            } catch (LastFmException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toMap(t -> t.left, t -> t.right, (f, s) -> f));
    }

    private static record Holder(TrackInfo left, ScrobbledTrack right) {

    }
}
