package core.commands.whoknows;

import core.apis.last.entities.TrackExtended;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.Optionals;
import core.services.validators.ArtistValidator;
import core.services.validators.TrackValidator;
import dao.ServiceView;
import dao.entities.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LocalWhoKnowsSongCommand extends LocalWhoKnowsAlbumCommand {

    public LocalWhoKnowsSongCommand(ServiceView dao) {
        super(dao);
    }


    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        ArtistSongParser parser = new ArtistSongParser(db, lastFM, false, Optionals.LIST.opt
                , Optionals.PIE.opt);
        parser.setExpensiveSearch(true);
        return parser;
    }

    @Override
    WrapperReturnNowPlaying generateWrapper(ArtistAlbumParameters ap, WhoKnowsMode whoKnowsMode) throws LastFmException {
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, ap.getE()).validate(ap.getArtist(), !ap.isNoredirect());
        ap.setScrobbledArtist(sA);
        String artist = sA.getArtist();

        ScrobbledArtist who = ap.getScrobbledArtist();
        long artistId = who.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);
        ScrobbledTrack sT = new TrackValidator(db, lastFM).validate(who.getArtistId(), who.getArtist(), ap.getAlbum());
        String track = sT.getName();
        long trackId = sT.getTrackId();

        if (trackId == -1) {
            sendMessageQueue(ap.getE(), "Couldn't confirm the song " + sT.getName() + " by " + sA.getArtist() + " exists :(");
            return null;
        }
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                effectiveMode.equals(WhoKnowsMode.IMAGE) ?
                this.db.getWhoKnowsTrack(10, trackId, ap.getE().getGuild().getIdLong()) :
                this.db.getWhoKnowsTrack(Integer.MAX_VALUE, trackId, ap.getE().getGuild().getIdLong());
        wrapperReturnNowPlaying.setArtist(ap.getScrobbledArtist().getArtist());
        try {
            TrackExtended trackInfo = lastFM.getTrackInfoExtended(ap.getLastFMData(), artist, track);
            if (trackInfo.getImageUrl() != null && !trackInfo.getImageUrl().isBlank()) {
                db.updateTrackImage(trackId, trackInfo.getImageUrl());
                wrapperReturnNowPlaying.setUrl(trackInfo.getImageUrl());
            }
            if (trackInfo.getPlays() > 0) {
                Optional<ReturnNowPlaying> any = wrapperReturnNowPlaying.getReturnNowPlayings().stream().filter(x -> x.getDiscordId() == ap.getLastFMData().getDiscordId()).findAny();
                if (any.isPresent()) {
                    any.get().setPlayNumber(trackInfo.getPlays());
                } else {
                    wrapperReturnNowPlaying.getReturnNowPlayings().add(new ReturnNowPlaying(ap.getLastFMData().getDiscordId(), ap.getLastFMData().getName(), artist, trackInfo.getPlays()));
                    wrapperReturnNowPlaying.setRows(wrapperReturnNowPlaying.getRows() + 1);

                }
                wrapperReturnNowPlaying.getReturnNowPlayings().sort(Comparator.comparingInt(ReturnNowPlaying::getPlayNumber).reversed());
            }
        } catch (LastFmException exception) {
            //Ignored
        }
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(ap.getE(), "No one knows " + CommandUtil.escapeMarkdown(who.getArtist() + " - " + track));
            return null;
        }
        wrapperReturnNowPlaying.setReturnNowPlayings(
                wrapperReturnNowPlaying.getReturnNowPlayings()
                        .stream().<ReturnNowPlaying>map(x -> new ReturnNowPlayingSong(x, track))
                        .peek(x -> x.setArtist(who.getArtist()))
                        .peek(x -> x.setDiscordName(CommandUtil.getUserInfoUnescaped(ap.getE(), x.getDiscordId()).username()))
                        .toList());


        wrapperReturnNowPlaying.setArtist(who.getArtist() + " - " + track);
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
