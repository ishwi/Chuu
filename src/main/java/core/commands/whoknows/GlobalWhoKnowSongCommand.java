package core.commands.whoknows;

import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.validators.ArtistValidator;
import core.services.validators.TrackValidator;
import core.util.ServiceView;
import dao.entities.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GlobalWhoKnowSongCommand extends GlobalBaseWhoKnowCommand<ArtistAlbumParameters> {
    public GlobalWhoKnowSongCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    LastFMData obtainLastFmData(ArtistAlbumParameters params) {
        return params.getLastFMData();
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistSongParser(db, lastFM, false);
    }

    @Override
    public String getDescription() {
        return "Like who knows song but for all bot users and keeping some privacy";
    }


    @Override
    public List<String> getAliases() {
        return Arrays.asList("gwktrack", "gwkt", "gwhoknowstrack", "gwt", "gwks", "gws");
    }


    @Override
    public String getName() {
        return "Global Who Knows Track";
    }



    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistAlbumParameters params, WhoKnowsDisplayMode whoKnowsDisplayMode) throws LastFmException {
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, params.getE()).validate(params.getArtist(), !params.isNoredirect());
        Context e = params.getE();
        long artistId = sA.getArtistId();
        params.setScrobbledArtist(sA);

        long trackId = new TrackValidator(db, lastFM).validate(sA.getArtistId(), sA.getArtist(), params.getAlbum()).getTrackId();
        ScrobbledAlbum fakeAlbum = new ScrobbledAlbum(trackId, null);
        fakeAlbum.setAlbum(params.getAlbum());

        params.setScrobbledAlbum(fakeAlbum);
        WhoKnowsDisplayMode effectiveMode = getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);

        boolean b = CommandUtil.showBottedAccounts(params.getLastFMData(), params, db);
        long author = params.getE().getAuthor().getIdLong();
        int limit = effectiveMode.equals(WhoKnowsDisplayMode.IMAGE) ? 10 : Integer.MAX_VALUE;
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                this.db.getGlobalWhoKnowsTrack(limit, trackId, author, b, hidePrivate(params));
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(params.getE(), "No one knows " + CommandUtil.escapeMarkdown(sA.getArtist() + " - " + params.getAlbum()));
            return null;
        }
        return wrapperReturnNowPlaying;
    }

    @Override
    public Optional<Rank<ReturnNowPlaying>> fetchNotInList(ArtistAlbumParameters ap, WrapperReturnNowPlaying wr) {
        ScrobbledAlbum sA = ap.getScrobbledAlbum();
        boolean showBotted = CommandUtil.showBottedAccounts(ap.getLastFMData(), ap, db);
        List<GlobalCrown> globals = db.getGlobalTrackRanking(sA.getAlbumId(), showBotted, ap.getE().getAuthor().getIdLong());
        Optional<GlobalCrown> yourPosition = globals.stream().filter(x -> x.getDiscordId() == ap.getLastFMData().getDiscordId()).findFirst();
        return yourPosition.map(gc -> new Rank<>(
                new GlobalReturnNowPlayingSong(gc.getDiscordId(),
                        gc.getLastfmID(),
                        ap.getScrobbledArtist().getArtist(),
                        gc.getPlaycount(),
                        ap.getLastFMData().getPrivacyMode(), sA.getAlbum()), gc.getRanking() - 1));
    }

    @Override
    public String getTitle(ArtistAlbumParameters params, String ignored) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getArtist() + " - " + params.getAlbum()) + " in " + params.getE().getJDA().getSelfUser().getName() + "?";
    }
}
