package core.commands.stats;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ResultWrapper;
import dao.entities.ScrobbledArtist;
import dao.entities.UserArtistComparison;

import java.util.List;

public class TasteTrackCommand extends TasteArtistCommand {
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;

    public TasteTrackCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotifyApi = SpotifySingleton.getInstance();
        this.thumbnailPerRow = true;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public String getEntity(ArtistParameters params) {
        return params.getScrobbledArtist().getArtist() + " tracks";
    }


    @Override
    public String getDescription() {
        return "Compare Your musical taste with another user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("tastetrack", "tastetrack", "tastetr", "comparetr");
    }


    @Override
    public ResultWrapper<UserArtistComparison> getResult(LastFMData og, LastFMData second, ArtistParameters params) throws LastFmException {
        boolean isList = params.hasOptional("list");
        String artist = params.getArtist();
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        params.setScrobbledArtist(scrobbledArtist);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotifyApi);
        return getService().getSimilaritiesTracks(List.of(og.getName(), second.getName()), scrobbledArtist.getArtistId(), isList ? 200 : Integer.MAX_VALUE);
    }


    @Override
    public String getName() {
        return "Taste track";
    }

}
