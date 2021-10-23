package core.commands.stats;

import core.commands.Context;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.Optionals;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public class TasteAlbumCommand extends BaseTasteCommand<ArtistAlbumParameters> {

    public TasteAlbumCommand(ServiceView dao) {
        super(dao);
        this.thumbnailPerRow = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public String getEntity(ArtistAlbumParameters params) {
        return params.getAlbum() + " tracks";
    }

    @Override
    public @Nullable
    String hasCustomUrl(ArtistAlbumParameters params) {
        return params.getScrobbledArtist().getUrl() != null && !params.getScrobbledArtist().getUrl().isBlank() ? params.getScrobbledArtist().getUrl() : null;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistAlbumParser(db, lastFM, Optionals.LIST.opt);
    }

    @Override
    public String getDescription() {
        return "Compare your top tracks of an album with another user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("tastealbum", "tastealb", "tasteal", "compal", "compareal");
    }


    @Override
    public Pair<LastFMData, LastFMData> getUserDatas(Context e, ArtistAlbumParameters params) throws InstanceNotFoundException {
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
    public ResultWrapper<UserArtistComparison> getResult(LastFMData og, LastFMData second, ArtistAlbumParameters params) throws LastFmException {
        boolean isList = params.hasOptional("list");
        String artist = params.getArtist();
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, params.getE()).validate(artist, true, !params.isNoredirect());
        params.setScrobbledArtist(sA);

        long albumId = CommandUtil.albumvalidate(db, sA, lastFM, params.getAlbum()).id();
        return db.getSimilaritiesAlbumTracks(List.of(og.getName(), second.getName()), albumId, isList ? 200 : Integer.MAX_VALUE);
    }

    @Override
    public Pair<Integer, Integer> getTasteBar(ResultWrapper<UserArtistComparison> resultWrapper, UserInfo og, UserInfo second, ArtistAlbumParameters params) {
        return Pair.of(resultWrapper.getResultList().stream().mapToInt(UserArtistComparison::getCountA).sum(), resultWrapper.getResultList().stream().mapToInt(UserArtistComparison::getCountB).sum());

    }


    @Override
    public String getName() {
        return "Taste Album";
    }
}
