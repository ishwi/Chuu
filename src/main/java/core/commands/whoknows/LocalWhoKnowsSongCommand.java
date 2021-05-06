package core.commands.whoknows;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.entities.TrackExtended;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LocalWhoKnowsSongCommand extends LocalWhoKnowsAlbumCommand {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public LocalWhoKnowsSongCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }


    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        ArtistSongParser parser = new ArtistSongParser(db, lastFM, new OptionalEntity("list", "display in list format")
                , new OptionalEntity("pie", "display it as a chart pie"));
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistAlbumParameters ap, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist validable = new ScrobbledArtist(ap.getArtist(), 0, "");
        CommandUtil.validate(db, validable, lastFM, discogsApi, spotify, false, !ap.isNoredirect());
        ap.setScrobbledArtist(validable);
        ScrobbledArtist who = ap.getScrobbledArtist();
        long artistId = who.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);
        long trackId = CommandUtil.trackValidate(db, ap
                .getScrobbledArtist(), lastFM, ap.getAlbum());
        if (trackId == -1) {
            sendMessageQueue(ap.getE(), "Couldn't confirm the song " + ap.getAlbum() + " by " + ap.getScrobbledArtist().getArtist() + " exists :(");
            return null;
        }
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                effectiveMode.equals(WhoKnowsMode.IMAGE) ?
                        this.db.getWhoKnowsTrack(10, trackId, ap.getE().getGuild().getIdLong()) :
                        this.db.getWhoKnowsTrack(Integer.MAX_VALUE, trackId, ap.getE().getGuild().getIdLong());
        wrapperReturnNowPlaying.setArtist(ap.getScrobbledArtist().getArtist());
        try {
            TrackExtended trackInfo = lastFM.getTrackInfoExtended(ap.getLastFMData(), ap.getArtist(), ap.getAlbum());
            if (trackInfo.getImageUrl() != null && !trackInfo.getImageUrl().isBlank()) {
                db.updateTrackImage(trackId, trackInfo.getImageUrl());
                wrapperReturnNowPlaying.setUrl(trackInfo.getImageUrl());
            }
            if (trackInfo.getPlays() > 0) {
                Optional<ReturnNowPlaying> any = wrapperReturnNowPlaying.getReturnNowPlayings().stream().filter(x -> x.getDiscordId() == ap.getLastFMData().getDiscordId()).findAny();
                if (any.isPresent()) {
                    any.get().setPlayNumber(trackInfo.getPlays());
                } else {
                    wrapperReturnNowPlaying.getReturnNowPlayings().add(new ReturnNowPlaying(ap.getLastFMData().getDiscordId(), ap.getLastFMData().getName(), ap.getArtist(), trackInfo.getPlays()));
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
        wrapperReturnNowPlaying.setReturnNowPlayings(
                wrapperReturnNowPlaying.getReturnNowPlayings()
                        .stream().<ReturnNowPlaying>map(x -> new ReturnNowPlayingAlbum(x, ap.getAlbum()))
                        .peek(x -> x.setArtist(who.getArtist()))
                        .peek(x -> x.setDiscordName(CommandUtil.getUserInfoNotStripped(ap.getE(), x.getDiscordId()).getUsername()))
                        .toList());


        wrapperReturnNowPlaying.setArtist(who.getArtist() + " - " + ap.getAlbum());
        return wrapperReturnNowPlaying;
    }


    @Override
    public String getDescription() {
        return "Get the list of people that have played a specific song on this server";
    }

    @Override
    public String getName() {
        return "Who knows song";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("wktrack", "whoknowstrack", "wkt", "wks", "wt", "ws");
    }
}
