package core.services.validators;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.GuildProperties;
import dao.entities.ScrobbledArtist;
import dao.entities.UpdaterStatus;
import dao.exceptions.InstanceNotFoundException;

import java.util.function.Supplier;

public record ArtistValidator(ChuuService dao, ConcurrentLastFM lastFM, Context context) {
    private static final DiscogsApi discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
    private static final Spotify spotifyApi = SpotifySingleton.getInstance();

    private String getUrl(long artistId, String replacement) {
        Supplier<String> filler = () -> dao.findArtistUrlAbove(artistId, 10).orElse(replacement);
        if (context.isFromGuild()) {
            try {
                long guildId = context.getGuild().getIdLong();
                GuildProperties guildProperties = dao.getGuildProperties(guildId);
                if (true) {
                    // TODO Server own images
                    return filler.get();
//                            dao.getServerArtistUrl(artistId, guildId, 10);
                }
            } catch (InstanceNotFoundException e) {
                return filler.get();
            }
        }
        return filler.get();
    }

    public ScrobbledArtist validate(String artist) throws LastFmException {
        return validate(artist, true, true);
    }

    public ScrobbledArtist validate(String artist, boolean findCorrection) throws LastFmException {
        return validate(artist, true, findCorrection);
    }

    public ScrobbledArtist validate(String artist, boolean doUrlCheck, boolean findCorrection) throws LastFmException {
        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        if (findCorrection) {
            String dbCorrection = dao.findCorrection(artist);
            if (dbCorrection != null) {
                scrobbledArtist.setArtist(dbCorrection);
            }
        }
        boolean existed;
        boolean corrected = false;
        UpdaterStatus status = null;
        try {
            status = dao.getUpdaterStatusByName(scrobbledArtist.getArtist());
            scrobbledArtist.setArtistId(status.getArtistId());
            scrobbledArtist.setArtist(status.getArtistName());
            existed = true;
        } catch (InstanceNotFoundException e) {
            //Artist doesnt exists
            String originalArtist = scrobbledArtist.getArtist();
            String correction = lastFM.getCorrection(originalArtist);
            if (!correction.equalsIgnoreCase(originalArtist)) {
                scrobbledArtist.setArtist(correction);
                corrected = true;
            }
            try {
                status = dao.getUpdaterStatusByName(correction);
                scrobbledArtist.setArtistId(status.getArtistId());
                scrobbledArtist.setArtist(status.getArtistName());
                existed = true;
            } catch (InstanceNotFoundException ex) {
                scrobbledArtist.setArtist(correction);
                //Mutates id
                dao.upsertArtistSad(scrobbledArtist);
                existed = false;
            }
            if (corrected) {
                dao.insertCorrection(scrobbledArtist.getArtistId(), originalArtist);
            }


        }
        if (doUrlCheck) {
            if (!existed || (status.getArtistUrl() == null)) {
                if (discogsApi != null && spotifyApi != null) {
                    scrobbledArtist.setUrl(CommandUtil.updateUrl(discogsApi, scrobbledArtist, dao, spotifyApi));
                } else {
                    scrobbledArtist.setUrl(null);
                    return scrobbledArtist;
                }
            } else {
                scrobbledArtist.setUrl(
                        getUrl(scrobbledArtist.getArtistId(), status.getArtistUrl())
                );
            }
            if (scrobbledArtist.getUrl() == null || scrobbledArtist.getUrl().isBlank()) {
                scrobbledArtist.setUrl(null);
            }
        }
        return scrobbledArtist;

    }

}
