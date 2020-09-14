package core.commands;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.imagerenderer.WhoKnowsMaker;
import core.imagerenderer.util.IPieableList;
import core.imagerenderer.util.PieableListKnows;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistAlbumParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static core.commands.WhoKnowsCommand.getEffectiveMode;

public class LocalWhoKnowsAlbumCommand extends WhoKnowsBaseCommand<ArtistAlbumParameters> {


    private final Spotify spotify;
    private final DiscogsApi discogsApi;
    private final IPieableList<ReturnNowPlaying, ArtistParameters> pie;

    public LocalWhoKnowsAlbumCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
        this.pie = new PieableListKnows<>(this.parser);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.SERVER_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> getParser() {
        ArtistAlbumParser parser = new ArtistAlbumParser(getService(), lastFM, new OptionalEntity("list", "display in list format"));
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    public String getDescription() {
        return ("How many times the guild has heard an album! (Using local database)");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("wkalbum", "wka", "whoknowsalbum");
    }

    @Override
    public String getName() {
        return "Get local guild Album plays";
    }


    @Override
    WhoKnowsMode getWhoknowsMode(ArtistAlbumParameters params) {
        return getEffectiveMode(params.getLastFMData().getWhoKnowsMode(), params);
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistAlbumParameters ap, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist validable = new ScrobbledArtist(ap.getArtist(), 0, "");
        CommandUtil.validate(getService(), validable, lastFM, discogsApi, spotify, false, !ap.isNoredirect());
        ap.setScrobbledArtist(validable);
        ScrobbledArtist who = ap.getScrobbledArtist();
        long artistId = who.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);
        long albumId = CommandUtil.albumvalidate(getService(), ap
                .getScrobbledArtist(), lastFM, ap.getAlbum());
        if (albumId == -1) {
            sendMessageQueue(ap.getE(), "Couldn't confirm the album " + ap.getAlbum() + " by " + ap.getScrobbledArtist().getArtist() + " exists :(");
            return null;
        }
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                effectiveMode.equals(WhoKnowsMode.IMAGE) ? this.getService().getWhoKnowsAlbums(10, albumId, ap.getE().getGuild().getIdLong()) : this.getService().getWhoKnowsAlbums(Integer.MAX_VALUE, albumId, ap.getE().getGuild().getIdLong());
        wrapperReturnNowPlaying.setArtist(ap.getScrobbledArtist().getArtist() + " - " + ap.getAlbum());
        try {

            AlbumUserPlays playsAlbumArtist = lastFM.getPlaysAlbumArtist(ap.getLastFMData().getName(), ap.getArtist(), ap.getAlbum());
            if (playsAlbumArtist.getAlbumUrl() != null && !playsAlbumArtist.getAlbumUrl().isBlank()) {
                getService().updateAlbumImage(albumId, playsAlbumArtist.getAlbumUrl());
                wrapperReturnNowPlaying.setUrl(playsAlbumArtist.getAlbumUrl());
            }
            if (playsAlbumArtist.getPlays() > 0) {
                Optional<ReturnNowPlaying> any = wrapperReturnNowPlaying.getReturnNowPlayings().stream().filter(x -> x.getDiscordId() == ap.getLastFMData().getDiscordId()).findAny();
                if (any.isPresent()) {
                    any.get().setPlayNumber(playsAlbumArtist.getPlays());
                } else {
                    wrapperReturnNowPlaying.getReturnNowPlayings().add(new ReturnNowPlaying(ap.getLastFMData().getDiscordId(), ap.getLastFMData().getName(), ap.getArtist() + " - " + ap.getAlbum(), playsAlbumArtist.getPlays()));
                    wrapperReturnNowPlaying.setRows(wrapperReturnNowPlaying.getRows() + 1);

                }
                wrapperReturnNowPlaying.getReturnNowPlayings().sort(Comparator.comparingInt(ReturnNowPlaying::getPlayNumber).reversed());
            }
        } catch (LastFmException exception) {
            //Ignored
        }
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(ap.getE(), "No one knows " + CommandUtil.cleanMarkdownCharacter(who.getArtist() + " - " + ap.getAlbum()));
            return null;
        }
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x -> x.setDiscordName(CommandUtil.getUserInfoNotStripped(ap.getE(), x.getDiscordId()).getUsername()));

        return wrapperReturnNowPlaying;
    }


    @Override
    public String getTitle(ArtistAlbumParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.cleanMarkdownCharacter(params.getArtist() + " - " + params.getAlbum()) + " in " + baseTitle + "?";
    }

}
