package core.commands.whoknows;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;

import java.util.Arrays;
import java.util.List;


public class WhoKnowsCommand extends WhoKnowsBaseCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public WhoKnowsCommand(ServiceView dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();


    }


    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM, false,
                new OptionalEntity("list", "display in list format"));
    }

    @Override
    public String getDescription() {
        return "Users who know the given artist";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("whoknows", "wk", "w");
    }


    @Override
    WhoKnowsMode getWhoknowsMode(ArtistParameters ap) {
        return getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify, true, !params.isNoredirect());
        params.setScrobbledArtist(scrobbledArtist);
        Context e = params.getE();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                whoKnowsMode.equals(WhoKnowsMode.IMAGE) ? this.db.whoKnows(scrobbledArtist.getArtistId(), e.getGuild().getIdLong()) : this.db.whoKnows(scrobbledArtist.getArtistId(), e.getGuild().getIdLong(), Integer.MAX_VALUE);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(e, "No one knows " + CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist()));
            return null;
        }
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x -> x.setDiscordName(CommandUtil.getUserInfoNotStripped(e, x.getDiscordId()).getUsername()));
        wrapperReturnNowPlaying.setUrl(scrobbledArtist.getUrl());
        return wrapperReturnNowPlaying;
    }

    @Override
    public String getTitle(ArtistParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.cleanMarkdownCharacter(params.getScrobbledArtist().getArtist()) + " in " + baseTitle + "?";
    }


    @Override
    public String getName() {
        return "Who Knows";
    }


}
