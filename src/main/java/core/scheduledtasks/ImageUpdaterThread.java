package core.scheduledtasks;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.exceptions.DiscogsServiceException;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;

import java.util.Set;

/**
 * Searches the artists wiht null urls
 * Note that after this method has run unless a discogs expection occurred the url will be set to either the image found or to a new state that will allow spotify to search for a new image
 */
public class ImageUpdaterThread implements Runnable {
    private final ChuuService dao;
    private final DiscogsApi discogsApi;

    public ImageUpdaterThread(ChuuService dao) {
        this.dao = dao;
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();

    }

    @Override
    public void run() {
        Set<ScrobbledArtist> artistData = dao.getNullUrls();
        System.out.println("Found at lest " + artistData.size() + " null artist ");
        for (ScrobbledArtist artistDatum : artistData) {
            String url;
            System.out.println("Working with artist " + artistDatum.getArtist());
            try {
                //We can get rate limited if we do it wihtout sleeping
                Thread.sleep(100L);
                url = discogsApi.findArtistImage(artistDatum.getArtist());

                if (url == null || url.isEmpty()) {
                    dao.updateImageStatus(artistDatum.getArtistId(), "", true);
                } else {
                    dao.upsertUrl(url, artistDatum.getArtistId());
                    System.out.println("Upserting buddy");
                }
            } catch (DiscogsServiceException | InterruptedException e) {
                Chuu.getLogger().warn(e.getMessage(), e);
            }
        }
    }


}
