package core.scheduledtasks;

import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import dao.ChuuService;
import dao.entities.ArtistInfo;

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
        this.spotifyApi = SpotifySingleton.getInstanceUsingDoubleLocking();

    }

    @Override
    public void run() {
        Set<String> artistData = dao.getSpotifyNulledUrls();
        System.out.println("Found at lest Spotify " + artistData.size() + "null artist ");
        for (String artistDatum : artistData) {
            String url;
            System.out.println("Working with artist " + artistDatum);

            url = spotifyApi.getArtistUrlImage(artistDatum);
            if (url != null) {
                if (!url.isEmpty())
                    System.out.println("INSERTED : " + artistDatum);
                dao.upsertSpotify(new ArtistInfo(url, artistDatum));
            }

        }


    }


}
