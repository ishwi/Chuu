package core.apis.last;

import dao.exceptions.ChuuServiceException;

import java.io.InputStream;
import java.util.Properties;

public class LastFMFactory {


    private LastFMFactory() {

    }

    public static ConcurrentLastFM getNewInstance() {
        Properties properties = new Properties();
        try (InputStream in = LastFMFactory.class.getResourceAsStream("/" + "all.properties")) {
            properties.load(in);
            String apikey = properties.getProperty("LASTFM_APIKEY");
            String secret = properties.getProperty("LASTFM_APISECRET");
            return new ConcurrentLastFM(apikey, secret);
        } catch (Exception e) {
            throw new ChuuServiceException(e);
        }

    }
}
