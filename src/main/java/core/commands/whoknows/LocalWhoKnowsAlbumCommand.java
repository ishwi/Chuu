package core.commands.whoknows;

import core.Chuu;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.Optionals;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LocalWhoKnowsAlbumCommand extends WhoKnowsBaseCommand<ArtistAlbumParameters> {


    public static final List<String> WKA_ALIASES = Arrays.asList("wkalbum", "wka", "whoknowsalbum", "wa");

    public LocalWhoKnowsAlbumCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
    }


    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        ArtistAlbumParser parser = new ArtistAlbumParser(db, lastFM, false, Optionals.LIST.opt);
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    public String getDescription() {
        return ("How many times the guild has heard an album! (Using local database)");
    }

    @Override
    public List<String> getAliases() {
        return WKA_ALIASES;
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
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, ap.getE()).validate(ap.getArtist(), !ap.isNoredirect());
        ap.setScrobbledArtist(sA);
        ScrobbledArtist who = ap.getScrobbledArtist();
        long artistId = who.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);
        Album album = CommandUtil.albumvalidate(db, ap
                .getScrobbledArtist(), lastFM, ap.getAlbum());
        long albumId = album.id();
        if (albumId == -1) {
            sendMessageQueue(ap.getE(), "Couldn't confirm the album " + album.albumName() + " by " + sA.getArtist() + " exists :(");
            return null;
        }
        WrapperReturnNowPlaying wrapperReturnNowPlaying = generateInnerWrapper(ap, effectiveMode, albumId);
        wrapperReturnNowPlaying.setArtist(ap.getScrobbledArtist().getArtist());
        try {

            AlbumUserPlays playsAlbumArtist = lastFM.getPlaysAlbumArtist(ap.getLastFMData(), sA.getArtist(), album.albumName());
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
                .peek(x -> x.setDiscordName(CommandUtil.getUserInfoUnescaped(ap.getE(), x.getDiscordId()).username()))
                .toList());


        wrapperReturnNowPlaying.setArtist(who.getArtist() + " - " + ap.getAlbum());
        return wrapperReturnNowPlaying;
    }

    protected WrapperReturnNowPlaying generateInnerWrapper(ArtistAlbumParameters ap, WhoKnowsMode effectiveMode, long albumId) {
        return effectiveMode.equals(WhoKnowsMode.IMAGE) ?
                this.db.getWhoKnowsAlbums(10, albumId, ap.getE().getGuild().getIdLong()) :
                this.db.getWhoKnowsAlbums(Integer.MAX_VALUE, albumId, ap.getE().getGuild().getIdLong());
    }


    @Override
    public String getTitle(ArtistAlbumParameters params, String baseTitle) {
        return "Who knows " + CommandUtil.escapeMarkdown(params.getArtist() + " - " + params.getAlbum()) + " in " + baseTitle + "?";
    }

}
