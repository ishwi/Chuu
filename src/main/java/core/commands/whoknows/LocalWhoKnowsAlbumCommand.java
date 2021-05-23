package core.commands.whoknows;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ServiceView;
import dao.entities.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LocalWhoKnowsAlbumCommand extends WhoKnowsBaseCommand<ArtistAlbumParameters> {


    private final Spotify spotify;
    private final DiscogsApi discogsApi;

    public LocalWhoKnowsAlbumCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }


    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        ArtistAlbumParser parser = new ArtistAlbumParser(db, lastFM, false, new OptionalEntity("list", "display in list format"));
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    public String getDescription() {
        return ("How many times the guild has heard an album! (Using local database)");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("wkalbum", "wka", "whoknowsalbum", "wa");
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
        CommandUtil.validate(db, validable, lastFM, discogsApi, spotify, false, !ap.isNoredirect());
        ap.setScrobbledArtist(validable);
        ScrobbledArtist who = ap.getScrobbledArtist();
        long artistId = who.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);
        Album album = CommandUtil.albumvalidate(db, ap
                .getScrobbledArtist(), lastFM, ap.getAlbum());
        long albumId = album.id();
        if (albumId == -1) {
            sendMessageQueue(ap.getE(), "Couldn't confirm the album " + ap.getAlbum() + " by " + ap.getScrobbledArtist().getArtist() + " exists :(");
            return null;
        }
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                effectiveMode.equals(WhoKnowsMode.IMAGE) ?
                this.db.getWhoKnowsAlbums(10, albumId, ap.getE().getGuild().getIdLong()) :
                this.db.getWhoKnowsAlbums(Integer.MAX_VALUE, albumId, ap.getE().getGuild().getIdLong());
        wrapperReturnNowPlaying.setArtist(ap.getScrobbledArtist().getArtist());
        try {

            AlbumUserPlays playsAlbumArtist = lastFM.getPlaysAlbumArtist(ap.getLastFMData(), ap.getArtist(), ap.getAlbum());
            if (playsAlbumArtist.getAlbumUrl() != null && !playsAlbumArtist.getAlbumUrl().isBlank()) {
                db.updateAlbumImage(albumId, playsAlbumArtist.getAlbumUrl());
                wrapperReturnNowPlaying.setUrl(Chuu.getCoverService().getCover(album, ap.getE()));
            }
            if (playsAlbumArtist.getPlays() > 0) {
                Optional<ReturnNowPlaying> any = wrapperReturnNowPlaying.getReturnNowPlayings().stream().filter(x -> x.getDiscordId() == ap.getLastFMData().getDiscordId()).findAny();
                if (any.isPresent()) {
                    any.get().setPlayNumber(playsAlbumArtist.getPlays());
                } else {
                    wrapperReturnNowPlaying.getReturnNowPlayings().add(new ReturnNowPlaying(ap.getLastFMData().getDiscordId(), ap.getLastFMData().getName(), ap.getArtist(), playsAlbumArtist.getPlays()));
                    wrapperReturnNowPlaying.setRows(wrapperReturnNowPlaying.getRows() + 1);

                }
                wrapperReturnNowPlaying.getReturnNowPlayings().sort(Comparator.comparingInt(ReturnNowPlaying::getPlayNumber).reversed());
            }
        } catch (LastFmException exception) {
            //Ignored
        }
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(ap.getE(), "No one knows " + CommandUtil.escapeMarkdown(who.getArtist() + " - " + ap.getAlbum()));
            return null;
        }
        wrapperReturnNowPlaying.setReturnNowPlayings(wrapperReturnNowPlaying.getReturnNowPlayings().stream()
                .<ReturnNowPlaying>map(x -> new ReturnNowPlayingAlbum(x, ap.getAlbum()))
                .peek(x -> x.setArtist(who.getArtist()))
                .peek(x -> x.setDiscordName(CommandUtil.getUserInfoUnescaped(ap.getE(), x.getDiscordId()).getUsername()))
                .toList());


        wrapperReturnNowPlaying.setArtist(who.getArtist() + " - " + ap.getAlbum());
        return wrapperReturnNowPlaying;
    }


    @Override
    public String getTitle(ArtistAlbumParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getArtist() + " - " + params.getAlbum()) + " in " + baseTitle + "?";
    }

}
