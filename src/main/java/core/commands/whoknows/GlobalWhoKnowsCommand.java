package core.commands.whoknows;

import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;

import java.util.List;
import java.util.Optional;

public class GlobalWhoKnowsCommand extends GlobalBaseWhoKnowCommand<ArtistParameters> {

    public GlobalWhoKnowsCommand(ServiceView dao) {
        super(dao);

    }

    @Override
    LastFMData obtainLastFmData(ArtistParameters params) {
        return params.getLastFMData();
    }

    @Override
    public String getName() {
        return "Global Who Knows";
    }

    @Override
    public String getDescription() {
        return "Like who knows but for all bot users and keeping some privacy :flushed:";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalwhoknows", "gk", "gwk", "gw");
    }

    @Override
    WhoKnowsMode getWhoknowsMode(ArtistParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, params.getE()).validate(params.getArtist(), !params.isNoredirect());
        params.setScrobbledArtist(sA);
        Context e = params.getE();
        long artistId = sA.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);

        boolean b = CommandUtil.showBottedAccounts(params.getLastFMData(), params, db);

        long author = params.getE().getAuthor().getIdLong();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                effectiveMode.equals(WhoKnowsMode.IMAGE) ? this.db.globalWhoKnows(artistId, b, author, hidePrivate(params)) : this.db.globalWhoKnows(artistId, Integer.MAX_VALUE, b, author, hidePrivate(params));
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(params.getE(), "No one knows " + CommandUtil.escapeMarkdown(sA.getArtist()));
            return null;
        }
        wrapperReturnNowPlaying.setUrl(sA.getUrl());
        return wrapperReturnNowPlaying;
    }

    @Override
    public Optional<Rank<ReturnNowPlaying>> fetchNotInList(ArtistParameters ap, WrapperReturnNowPlaying wr) {
        ScrobbledArtist sA = ap.getScrobbledArtist();
        boolean showBotted = CommandUtil.showBottedAccounts(ap.getLastFMData(), ap, db);
        List<GlobalCrown> globals = db.getGlobalArtistRanking(sA.getArtistId(), showBotted, ap.getLastFMData().getDiscordId());
        Optional<GlobalCrown> yourPosition = globals.stream().filter(x -> x.getDiscordId() == ap.getLastFMData().getDiscordId()).findFirst();
        return yourPosition.map(gc -> new Rank<>(
                new GlobalReturnNowPlaying(gc.getDiscordId(),
                        gc.getLastfmID(),
                        ap.getScrobbledArtist().getArtist(),
                        gc.getPlaycount(),
                        ap.getLastFMData().getPrivacyMode()), gc.getRanking() - 1));

    }

    @Override
    public String getTitle(ArtistParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getScrobbledArtist().getArtist()) + " in " + params.getE().getJDA().getSelfUser().getName() + "?";
    }


    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM, false);
    }
}
