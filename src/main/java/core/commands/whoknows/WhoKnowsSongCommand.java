package core.commands.whoknows;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.Optionals;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WhoKnowsSongCommand extends WhoKnowsAlbumCommand {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public WhoKnowsSongCommand(ServiceView dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }


    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        ArtistSongParser parser = new ArtistSongParser(db, lastFM, true, Optionals.LIST.opt
                , Optionals.PIE.opt);
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    Map<UsersWrapper, Integer> fillPlayCounter(List<UsersWrapper> userList, String artist, String track, AlbumUserPlays fillWithUrl, Context e) throws LastFmException {
        Map<UsersWrapper, Integer> userMapPlays = new LinkedHashMap<>();
        UsersWrapper usersWrapper = userList.get(0);
        Track temp = lastFM.getTrackInfo(LastFMData.ofUserWrapper(usersWrapper), artist, track);
        userMapPlays.put(usersWrapper, temp.getPlays());
        fillWithUrl.setAlbumUrl(temp.getImageUrl());

        fillWithUrl.setAlbum(temp.getName());
        if (temp.getImageUrl() == null || temp.getImageUrl().isBlank()) {
            fillWithUrl.setAlbumUrl(new ArtistValidator(db, lastFM, e).validate(artist).getUrl());
        }
        userList.stream().skip(1).forEach(u -> {
            try {
                Track trackInfo = lastFM.getTrackInfo(LastFMData.ofUserWrapper(u), artist, track);
                userMapPlays.put(u, trackInfo.getPlays());
            } catch (LastFmException ex) {
                Chuu.getLogger().warn(ex.getMessage(), ex);
            }
        });
        return userMapPlays;
    }

    @Override
    void doExtraThings(List<ReturnNowPlaying> list2, long id, long artistId, String album) {
        // Does nothing
    }

    @Override
    public String getDescription() {
        return "Get the list of people that have played a specific song on this server";
    }

    @Override
    public String getName() {
        return "Who Knows Song";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("uwktrack", "uwhoknowstrack", "uwkt", "uwt", "uws", "uwks");
    }
}
