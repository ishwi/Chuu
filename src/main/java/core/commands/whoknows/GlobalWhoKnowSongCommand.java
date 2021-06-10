package core.commands.whoknows;

import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;

import java.util.Arrays;
import java.util.List;

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
    WhoKnowsMode getWhoknowsMode(ArtistAlbumParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistAlbumParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify, true, !params.isNoredirect());
        Context e = params.getE();
        long artistId = scrobbledArtist.getArtistId();
        params.setScrobbledArtist(scrobbledArtist);
        long trackId = CommandUtil.trackValidate(db, scrobbledArtist, lastFM, params.getAlbum());
        WhoKnowsMode effectiveMode = getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);

        boolean b = CommandUtil.showBottedAccounts(params.getLastFMData(), params, db);
        long author = params.getE().getAuthor().getIdLong();
        int limit = effectiveMode.equals(WhoKnowsMode.IMAGE) ? 10 : Integer.MAX_VALUE;
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                this.db.getGlobalWhoKnowsTrack(limit, trackId, author, b, hidePrivate(params));
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(params.getE(), "No one knows " + CommandUtil.escapeMarkdown(scrobbledArtist.getArtist() + " - " + params.getAlbum()));
            return null;
        }
        return wrapperReturnNowPlaying;
    }

    @Override
    public String getTitle(ArtistAlbumParameters params, String ignored) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getArtist() + " - " + params.getAlbum()) + " in " + params.getE().getJDA().getSelfUser().getName() + "?";
    }
}
