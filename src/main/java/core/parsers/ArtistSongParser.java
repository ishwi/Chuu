package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import core.parsers.explanation.StrictUserExplanation;
import core.parsers.explanation.TrackExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;

import java.util.List;

public class ArtistSongParser extends ArtistAlbumParser {


    public ArtistSongParser(ChuuService dao, ConcurrentLastFM lastFM, boolean forComparison, OptionalEntity... o) {
        super(dao, lastFM, forComparison, o);
        slashName = TrackExplanation.NAME;
    }

    public ArtistSongParser(ChuuService dao, ConcurrentLastFM lastFM, OptionalEntity... o) {
        super(dao, lastFM, o);
        slashName = TrackExplanation.NAME;
    }

    @Override
    ArtistAlbumParameters doSomethingWithNp(NowPlayingArtist np, LastFMData lastFMData, Context e) {
        return new ArtistAlbumParameters(e, np.artistName(), np.songName(), lastFMData);
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "You need to use - to separate artist and song!");
        errorMessages.put(8, "Need both the artist and the song!");
        errorMessages
                .put(7, "You need to add the escape character **\"\\\\\"** in the **\"-\"** that appear on the album or song.\n " +
                        "\tFor example: Artist - So**\\\\-**ng  ");

    }

    @Override
    public List<Explanation> getUsages() {
        TrackExplanation trackExplanation = new TrackExplanation();
        return List.of(trackExplanation.artist(), trackExplanation.song(), new StrictUserExplanation());
    }

}
