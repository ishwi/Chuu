package core.scheduledtasks;

import core.Chuu;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.apis.spotify.UrlAndId;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;

import java.util.Optional;
import java.util.Set;


/**
 * Searches the artists wiht null urls or that didnt find an image on discogs and tries to find new ones
 * Note that after this method has run unless a spotify expection occurred the url will be set to either the image found or to the empty string to represent that no image was found
 */
public class SpotifyUpdaterThread implements Runnable {
    private final ChuuService dao;
    private final Spotify spotifyApi;

    public SpotifyUpdaterThread(ChuuService dao) {
        this.dao = dao;
        this.spotifyApi = SpotifySingleton.getInstance();

    }

    @Override
    public void run() {
        Set<ScrobbledArtist> artistData = dao.getSpotifyNulledUrls();
        Chuu.getLogger().info("Searching for {} urls via Spotify", artistData.size());
        int counter = 0;
        for (ScrobbledArtist artistDatum : artistData) {
            String url;

            Optional<UrlAndId> urlAndId = spotifyApi.getUrlAndId(artistDatum.getArtist());
            if (urlAndId.isPresent()) {
                UrlAndId id = urlAndId.get();
                url = id.url();
                if (url != null) {
                    if (url.isEmpty()) {
                        artistDatum.setUrl("");
                        artistDatum.setUpdateBit(false);
                        dao.updateImageStatus(artistDatum.getArtistId(), "", false);
                    } else {
                        dao.upsertSpotify(url, artistDatum.getArtistId(), id.id());
                        counter++;
                    }
                }
            }

        }
        Chuu.getLogger().info("Found {} urls in spotify out of {}", counter, artistData.size());
    }
}
