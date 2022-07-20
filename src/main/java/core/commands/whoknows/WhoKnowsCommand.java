package core.commands.whoknows;

import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.utils.Optionals;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class WhoKnowsCommand extends WhoKnowsBaseCommand<ArtistParameters> {

    public static final List<String> WK_ALIASES = Arrays.asList("whoknows", "wk", "w");

    public WhoKnowsCommand(ServiceView dao) {
        super(dao);
    }


    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM, false,
                Optionals.LIST.opt);
    }

    @Override
    public String getDescription() {
        return "Users who know the given artist";
    }

    @Override
    public List<String> getAliases() {
        return WK_ALIASES;
    }


    @Override
    protected WrapperReturnNowPlaying generateWrapper(ArtistParameters params, WhoKnowsDisplayMode whoKnowsDisplayMode) throws LastFmException {
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, params.getE()).validate(params.getArtist(), !params.isNoredirect());
        params.setScrobbledArtist(sA);
        Context e = params.getE();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                whoKnowsDisplayMode.equals(WhoKnowsDisplayMode.IMAGE) ? this.db.whoKnows(sA.getArtistId(), e.getGuild().getIdLong()) : this.db.whoKnows(sA.getArtistId(), e.getGuild().getIdLong(), Integer.MAX_VALUE);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(e, "No one knows " + CommandUtil.escapeMarkdown(sA.getArtist()));
            return null;
        }

        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x -> x.setDiscordName(CommandUtil.getUserInfoUnescaped(e, x.getDiscordId()).username()));
        wrapperReturnNowPlaying.setUrl(sA.getUrl());
        return wrapperReturnNowPlaying;
    }

    @Override
    LastFMData obtainLastFmData(ArtistParameters ap) {
        return ap.getLastFMData();
    }

    @Override
    public Optional<Rank<ReturnNowPlaying>> fetchNotInList(ArtistParameters ap, WrapperReturnNowPlaying wr) {
        WrapperReturnNowPlaying wrp = this.db.whoKnows(ap.getScrobbledArtist().getArtistId(), ap.getE().getGuild().getIdLong(), Integer.MAX_VALUE);

        List<ReturnNowPlaying> returnNowPlayings = wrp.getReturnNowPlayings();
        for (int i = 0; i < returnNowPlayings.size(); i++) {
            ReturnNowPlaying returnNowPlaying = returnNowPlayings.get(i);
            if (returnNowPlaying.getDiscordId() == ap.getLastFMData().getDiscordId()) {
                return Optional.of(new Rank<>(returnNowPlaying, i));
            }
        }
        return Optional.empty();
    }

    @Override
    public String getTitle(ArtistParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getScrobbledArtist().getArtist()) + " in " + baseTitle + "?";
    }


    @Override
    public String getName() {
        return "Who Knows";
    }


}
