package core.commands.whoknows;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GlobalWhoKnowsAlbumCommand extends GlobalBaseWhoKnowCommand<ArtistAlbumParameters> {
    public GlobalWhoKnowsAlbumCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    LastFMData obtainLastFmData(ArtistAlbumParameters params) {
        return params.getLastFMData();
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistAlbumParser(db, lastFM, false);
    }

    @Override
    public String getDescription() {
        return "Like who knows album but for all bot users and keeping some privacy";
    }


    @Override
    public List<String> getAliases() {
        return Arrays.asList("gwkalbum", "gwka", "gwhoknowsalbum", "gwa");
    }


    @Override
    public String getName() {
        return "Global who knows album";
    }


    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistAlbumParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, params.getE()).validate(params.getArtist(), !params.isNoredirect());
        Context e = params.getE();
        long artistId = sA.getArtistId();
        params.setScrobbledArtist(sA);
        Album album = CommandUtil.albumvalidate(db, sA, lastFM, params.getAlbum());
        params.setScrobbledAlbum(new ScrobbledAlbum(album, sA.getArtist()));

        WhoKnowsMode effectiveMode = getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);

        boolean b = CommandUtil.showBottedAccounts(params.getLastFMData(), params, db);
        long author = params.getE().getAuthor().getIdLong();
        int limit = effectiveMode.equals(WhoKnowsMode.IMAGE) ? 10 : Integer.MAX_VALUE;
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                this.db.getGlobalWhoKnowsAlbum(limit, album.id(), author, b, hidePrivate(params));
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(params.getE(), "No one knows " + CommandUtil.escapeMarkdown(sA.getArtist() + " - " + album.albumName()));
            return null;
        }
        wrapperReturnNowPlaying.setUrl(Chuu.getCoverService().getCover(album, e));
        return wrapperReturnNowPlaying;
    }

    @Override
    public Optional<Rank<ReturnNowPlaying>> fetchNotInList(ArtistAlbumParameters ap, WrapperReturnNowPlaying wr) {
        ScrobbledAlbum sAlb = ap.getScrobbledAlbum();
        boolean showBotted = CommandUtil.showBottedAccounts(ap.getLastFMData(), ap, db);
        List<GlobalCrown> globals = db.getGlobalAlbumRanking(sAlb.getAlbumId(), showBotted, ap.getE().getAuthor().getIdLong());
        Optional<GlobalCrown> yourPosition = globals.stream().filter(x -> x.getDiscordId() == ap.getLastFMData().getDiscordId()).findFirst();
        return yourPosition.map(gc -> new Rank<>(
                new GlobalReturnNowPlayingAlbum(gc.getDiscordId(),
                        gc.getLastfmID(),
                        ap.getScrobbledArtist().getArtist(),
                        gc.getPlaycount(),
                        ap.getLastFMData().getPrivacyMode(),
                        sAlb.getAlbum()), gc.getRanking() - 1));

    }

    @Override
    public String getTitle(ArtistAlbumParameters params, String ignored) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getArtist() + " - " + params.getAlbum()) + " in " + params.getE().getJDA().getSelfUser().getName() + "?";
    }
}
