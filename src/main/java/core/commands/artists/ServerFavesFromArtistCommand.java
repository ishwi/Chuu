package core.commands.artists;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ServiceView;
import dao.entities.AlbumUserPlays;
import dao.entities.ScrobbledArtist;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

public class ServerFavesFromArtistCommand extends ConcurrentCommand<ArtistParameters> {

    private final DiscogsApi discogs;
    private final Spotify spotify;

    public ServerFavesFromArtistCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
        this.discogs = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Your favourite tracks from an artist";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("serverfavs", "serverfavourites", "serverfavorites", "sfavs");
    }

    @Override
    public String slashName() {
        return "favs";
    }

    @Override
    public String getName() {

        return "Favs in server";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        String artist = params.getArtist();
        ScrobbledArtist who = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(db, who, lastFM, discogs, spotify);
        String lastFmName = params.getLastFMData().getName();
        String validArtist = who.getArtist();
        List<AlbumUserPlays> songs = db.getServerTopArtistTracks(e.getGuild().getIdLong(), who.getArtistId(), Integer.MAX_VALUE);

        GlobalFavesFromArtistCommand.sendArtistFaves(e, who, validArtist, lastFmName, songs, e.getGuild().getName(), "in this server!", e.getGuild().getIconUrl());

    }
}
