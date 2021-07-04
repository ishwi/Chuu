package core.commands.stats;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.utils.OptionalEntity;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public class TasteArtistCommand extends BaseTasteCommand<ArtistParameters> {

    public TasteArtistCommand(ServiceView dao) {
        super(dao);
        this.thumbnailPerRow = true;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public String getEntity(ArtistParameters params) {
        return params.getScrobbledArtist().getArtist() + " albums";
    }

    @Override
    public @Nullable String hasCustomUrl(ArtistParameters params) {
        return params.getScrobbledArtist().getUrl() != null && !params.getScrobbledArtist().getUrl().isBlank() ? params.getScrobbledArtist().getUrl() : null;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM, new OptionalEntity("list", "display in a list format"));
    }

    @Override
    public String getDescription() {
        return "Compare your musical taste with another user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("tasteartist", "tastea", "tastear", "comparea");
    }


    @Override
    public Pair<LastFMData, LastFMData> getUserDatas(Context e, ArtistParameters params) throws InstanceNotFoundException {
        User author = params.getE().getAuthor();
        LastFMData secondUser = params.getLastFMData();
        if (author.getIdLong() == secondUser.getDiscordId()) {
            sendMessageQueue(e, "You need to provide at least one other user (ping,discord id,tag format, u:username or lfm:lastfm_name )");
            return null;
        }
        LastFMData ogData = db.findLastFMData(author.getIdLong());
        return Pair.of(ogData, secondUser);
    }

    @Override
    public ResultWrapper<UserArtistComparison> getResult(LastFMData og, LastFMData second, ArtistParameters params) throws LastFmException {
        boolean isList = params.hasOptional("list");

        ScrobbledArtist sA = new ArtistValidator(db, lastFM, params.getE()).validate(params.getArtist(), !params.isNoredirect());
        params.setScrobbledArtist(sA);

        return db.getSimilaritiesAlbum(List.of(og.getName(), second.getName()), sA.getArtistId(), isList ? 200 : Integer.MAX_VALUE);
    }

    @Override
    public Pair<Integer, Integer> getTasteBar(ResultWrapper<UserArtistComparison> resultWrapper, UserInfo og, UserInfo second, ArtistParameters params) {
        return Pair.of(resultWrapper.getResultList().stream().mapToInt(UserArtistComparison::getCountA).sum(), resultWrapper.getResultList().stream().mapToInt(UserArtistComparison::getCountB).sum());

    }


    @Override
    public String getName() {
        return "Taste Artist";
    }

}
