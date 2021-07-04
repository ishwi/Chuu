package core.commands.stats;

import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.ResultWrapper;
import dao.entities.ScrobbledArtist;
import dao.entities.UserArtistComparison;

import java.util.List;

public class TasteTrackCommand extends TasteArtistCommand {

    public TasteTrackCommand(ServiceView dao) {
        super(dao);
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
        return "Compare your musical taste with another user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("tastetrack", "tastetrack", "tastetr", "comparetr");
    }


    @Override
    public ResultWrapper<UserArtistComparison> getResult(LastFMData og, LastFMData second, ArtistParameters params) throws LastFmException {
        boolean isList = params.hasOptional("list");

        ScrobbledArtist sA = new ArtistValidator(db, lastFM, params.getE()).validate(params.getArtist(), !params.isNoredirect());
        params.setScrobbledArtist(sA);

        return db.getSimilaritiesTracks(List.of(og.getName(), second.getName()), sA.getArtistId(), isList ? 200 : Integer.MAX_VALUE);
    }


    @Override
    public String getName() {
        return "Taste track";
    }

}
