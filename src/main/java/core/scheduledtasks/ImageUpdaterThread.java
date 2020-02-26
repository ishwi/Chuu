package core.scheduledtasks;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.exceptions.DiscogsServiceException;
import dao.ChuuService;
import dao.entities.ArtistInfo;

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
        Set<String> artistData = dao.getNullUrls();
        System.out.println("Found at lest " + artistData.size() + "null artist ");
        for (String artistDatum : artistData) {
			String url;
			System.out.println("Working with artist " + artistDatum);
			try {
                //We can get rate limited if we do it wihtout sleeping
                Thread.sleep(100L);
                url = discogsApi.findArtistImage(artistDatum);
                if (url != null) {

                    System.out.println("Upserting buddy");
                    if (!url.isEmpty())
                        System.out.println(artistDatum);
                    dao.upsertUrl(new ArtistInfo(url, artistDatum));
                }
            } catch (DiscogsServiceException e) {
				Chuu.getLogger().warn(e.getMessage(), e);


			} catch (InterruptedException e) {
				Chuu.getLogger().warn(e.getMessage(), e);
			}
		}
	}


}
