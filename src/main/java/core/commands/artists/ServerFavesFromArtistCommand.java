package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.imagerenderer.util.pie.DefaultList;
import core.imagerenderer.util.pie.IPieableList;
import core.imagerenderer.util.pie.OptionalPie;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.params.ChuuDataParams;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.AlbumUserPlays;
import dao.entities.ScrobbledArtist;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ServerFavesFromArtistCommand extends ConcurrentCommand<ArtistParameters> {

    private final IPieableList<AlbumUserPlays, ChuuDataParams> pie;

    public ServerFavesFromArtistCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
        this.pie = DefaultList.fillPie(AlbumUserPlays::getPie, AlbumUserPlays::getPlays);
        new OptionalPie(this.getParser());

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
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();

        ScrobbledArtist who = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());

        String lastFmName = params.getLastFMData().getName();
        String validArtist = who.getArtist();
        List<AlbumUserPlays> songs = db.getServerTopArtistTracks(e.getGuild().getIdLong(), who.getArtistId(), Integer.MAX_VALUE);

        GlobalFavesFromArtistCommand.sendArtistFaves(e, who, validArtist, lastFmName, songs, e.getGuild().getName(), "in this server!", e.getGuild().getIconUrl(), params, pie, (b) -> sendImage(b, e));

    }
}
