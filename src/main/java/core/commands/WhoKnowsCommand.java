package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.WhoKnowsMaker;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.imgscalr.Scalr;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;


public class WhoKnowsCommand extends WhoKnowsBaseCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public WhoKnowsCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();

    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<ArtistParameters> getParser() {
        return new ArtistParser(getService(), lastFM,
                new OptionalEntity("list", "display in list format"));
    }

    @Override
    public String getDescription() {
        return "Users who know the given artist";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("whoknows", "wk", "whoknowsnp", "wknp");
    }


    @Override
    WhoKnowsMode getWhoknowsMode(ArtistParameters ap) {
        return getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify, true, !params.isNoredirect());
        params.setScrobbledArtist(scrobbledArtist);
        MessageReceivedEvent e = params.getE();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                whoKnowsMode.equals(WhoKnowsMode.IMAGE) ? this.getService().whoKnows(scrobbledArtist.getArtistId(), e.getGuild().getIdLong()) : this.getService().whoKnows(scrobbledArtist.getArtistId(), e.getGuild().getIdLong(), Integer.MAX_VALUE);
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
