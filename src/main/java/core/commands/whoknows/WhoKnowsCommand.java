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
import dao.entities.ScrobbledArtist;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;

import java.util.Arrays;
import java.util.List;


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
    WhoKnowsMode getWhoknowsMode(ArtistParameters ap) {
        return getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);
    }

    @Override
    protected WrapperReturnNowPlaying generateWrapper(ArtistParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, params.getE()).validate(params.getArtist(), !params.isNoredirect());
        params.setScrobbledArtist(sA);
        Context e = params.getE();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                whoKnowsMode.equals(WhoKnowsMode.IMAGE) ? this.db.whoKnows(sA.getArtistId(), e.getGuild().getIdLong()) : this.db.whoKnows(sA.getArtistId(), e.getGuild().getIdLong(), Integer.MAX_VALUE);
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
    public String getTitle(ArtistParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getScrobbledArtist().getArtist()) + " in " + baseTitle + "?";
    }


    @Override
    public String getName() {
        return "Who Knows";
    }


}
